package com.blackbox.ai.data.model

import com.blackbox.ai.util.DebugLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL

object KiwixCatalogClient {

    private const val OPDS_URL = "https://library.kiwix.org/catalog/v2/entries"

    data class CatalogEntry(
        val id: String,
        val title: String,
        val description: String,
        val language: String,
        val languageName: String,
        val size: Long,
        val articleCount: Long,
        val mediaCount: Long,
        val date: String,
        val creator: String,
        val publisher: String,
        val name: String,
        val flavour: String,
        val tags: List<String>,
        val url: String,
        val faviconUrl: String?
    )

    suspend fun fetchCatalog(
        query: String? = null,
        language: String? = null,
        count: Int = 50,
        start: Int = 0
    ): Result<List<CatalogEntry>> = withContext(Dispatchers.IO) {
        try {
            val urlBuilder = StringBuilder(OPDS_URL)
            urlBuilder.append("?count=$count&start=$start")

            query?.takeIf { it.isNotBlank() }?.let {
                urlBuilder.append("&q=${java.net.URLEncoder.encode(it, "UTF-8")}")
            }

            language?.takeIf { it.isNotBlank() && it != "all" }?.let {
                urlBuilder.append("&lang=$it")
            }

            DebugLog.log("[KIWIX] Fetching catalog: $urlBuilder")

            val connection = URL(urlBuilder.toString()).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/atom+xml,application/json")
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true

            val httpCode = connection.responseCode
            val stream = if (httpCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val body = stream.bufferedReader().readText()
            DebugLog.log("[KIWIX] HTTP $httpCode, body bytes=${body.length}")

            val entries = when (httpCode) {
                in 200..299 -> {
                    val contentType = connection.contentType ?: ""
                    when {
                        contentType.contains("json", true) || body.trimStart().startsWith("{") -> {
                            parseJsonCatalog(body)
                        }
                        else -> parseOpdsXml(body)
                    }
                }
                else -> {
                    DebugLog.log("[KIWIX] HTTP error $httpCode: ${body.take(200)}")
                    return@withContext Result.failure(Exception("Kiwix catalog HTTP $httpCode"))
                }
            }

            DebugLog.log("[KIWIX] Fetched ${entries.size} entries")
            Result.success(entries)
        } catch (e: Exception) {
            DebugLog.log("[KIWIX] Catalog fetch error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun parseJsonCatalog(json: String): List<CatalogEntry> {
        val entries = mutableListOf<CatalogEntry>()
        return try {
            val root = JSONObject(json)
            val results = root.optJSONObject("results") ?: root
            val items = results.optJSONArray("entries")
                ?: results.optJSONArray("items")
                ?: results.optJSONArray("entry")
                ?: results.optJSONArray("data")

            val count = items?.length() ?: 0
            for (i in 0 until count) {
                val obj = items?.getJSONObject(i) ?: continue
                val id = obj.optString("id", obj.optString("_id", ""))
                val title = obj.optString("title", obj.optString("name", "Unknown"))
                val description = obj.optString("description", obj.optString("summary", ""))
                val language = obj.optString("language", "en")
                val url = obj.optString("url", obj.optString("download_url", ""))
                val size = obj.optString("size", obj.optString("filesize", "0")).toLongOrNull() ?: 0L

                if (url.isNotBlank()) {
                    entries.add(
                        CatalogEntry(
                            id = id,
                            title = title,
                            description = description,
                            language = language,
                            languageName = getLanguageName(language),
                            size = size,
                            articleCount = 0,
                            mediaCount = 0,
                            date = obj.optString("updated", obj.optString("date", "")),
                            creator = obj.optString("author", obj.optString("creator", "")),
                            publisher = obj.optString("publisher", "openZIM"),
                            name = obj.optString("name", ""),
                            flavour = obj.optString("flavour", ""),
                            tags = obj.optString("tags", "").split(";").filter { it.isNotBlank() },
                            url = url,
                            faviconUrl = null
                        )
                    )
                }
            }
            entries
        } catch (e: Exception) {
            DebugLog.log("[KIWIX] JSON parse error: ${e.message}")
            emptyList()
        }
    }

    private fun parseOpdsXml(xml: String): List<CatalogEntry> {
        val entries = mutableListOf<CatalogEntry>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var currentEntry: MutableMap<String, String>? = null
            var currentLinks = mutableListOf<Map<String, String>>()
            var currentTag = ""
            var inEntry = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        val tagName = parser.name

                        when (tagName) {
                            "entry" -> {
                                inEntry = true
                                currentEntry = mutableMapOf()
                                currentLinks = mutableListOf()
                            }
                            "link" -> {
                                if (inEntry) {
                                    val linkMap = mutableMapOf<String, String>()
                                    for (i in 0 until parser.attributeCount) {
                                        linkMap[parser.getAttributeName(i)] = parser.getAttributeValue(i)
                                    }
                                    currentLinks.add(linkMap)
                                }
                            }
                            "author", "publisher" -> {
                                currentTag = tagName
                            }
                            else -> {
                                if (inEntry) {
                                    currentTag = tagName
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inEntry && currentEntry != null) {
                            val text = parser.text?.trim() ?: ""
                            if (text.isNotEmpty()) {
                                when (currentTag) {
                                    "id" -> currentEntry["id"] = text
                                    "title" -> currentEntry["title"] = text
                                    "summary" -> currentEntry["summary"] = text
                                    "language" -> currentEntry["language"] = text
                                    "name" -> {
                                        if (!currentEntry.containsKey("name")) {
                                            currentEntry["name"] = text
                                        }
                                    }
                                    "flavour" -> currentEntry["flavour"] = text
                                    "tags" -> currentEntry["tags"] = text
                                    "articleCount" -> currentEntry["articleCount"] = text
                                    "mediaCount" -> currentEntry["mediaCount"] = text
                                    "updated" -> currentEntry["updated"] = text
                                    "author" -> currentEntry["author"] = (currentEntry["author"] ?: "") + text
                                    "publisher" -> currentEntry["publisher"] = (currentEntry["publisher"] ?: "") + text
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        val tagName = parser.name

                        when (tagName) {
                            "entry" -> {
                                if (currentEntry != null) {
                                    var downloadUrl = ""
                                    var faviconUrl: String? = null
                                    var size = 0L

                                    for (link in currentLinks) {
                                        val type = link["type"] ?: ""
                                        val rel = link["rel"] ?: ""
                                        val href = link["href"] ?: ""

                                        if (type.contains("application/x-zim") || href.endsWith(".zim") || href.contains(".zim")) {
                                            downloadUrl = href.replace(".meta4", "")
                                            size = link["length"]?.toLongOrNull() ?: 0L
                                        }
                                        if (rel.contains("thumbnail") || rel.contains("image")) {
                                            faviconUrl = if (href.startsWith("/")) "https://library.kiwix.org$href" else href
                                        }
                                    }

                                    if (downloadUrl.isNotBlank()) {
                                        val lang = currentEntry["language"] ?: "en"
                                        DebugLog.log("[KIWIX] Entry: ${currentEntry["title"]}, Size: $size, URL: ${downloadUrl.take(50)}...")

                                        entries.add(
                                            CatalogEntry(
                                                id = currentEntry["id"] ?: "",
                                                title = currentEntry["title"] ?: "Unknown",
                                                description = currentEntry["summary"] ?: "",
                                                language = lang,
                                                languageName = getLanguageName(lang),
                                                size = size,
                                                articleCount = currentEntry["articleCount"]?.toLongOrNull() ?: 0,
                                                mediaCount = currentEntry["mediaCount"]?.toLongOrNull() ?: 0,
                                                date = currentEntry["updated"] ?: "",
                                                creator = currentEntry["author"] ?: "",
                                                publisher = currentEntry["publisher"] ?: "openZIM",
                                                name = currentEntry["name"] ?: "",
                                                flavour = currentEntry["flavour"] ?: "",
                                                tags = currentEntry["tags"]?.split(";") ?: emptyList(),
                                                url = downloadUrl,
                                                faviconUrl = faviconUrl
                                            )
                                        )
                                    }
                                }
                                inEntry = false
                                currentEntry = null
                                currentLinks = mutableListOf()
                            }
                            "author", "publisher" -> {
                                currentTag = ""
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            DebugLog.log("[KIWIX] XML Parse error: ${e.message}")
            e.printStackTrace()
        }

        return entries
    }

    private fun getLanguageName(code: String): String = when (code.lowercase()) {
        "eng", "en" -> "English"
        "spa", "es" -> "Spanish"
        "fra", "fr" -> "French"
        "deu", "de" -> "German"
        "ita", "it" -> "Italian"
        "por", "pt" -> "Portuguese"
        "rus", "ru" -> "Russian"
        "zho", "zh" -> "Chinese"
        "jpn", "ja" -> "Japanese"
        "ara", "ar" -> "Arabic"
        "hin", "hi" -> "Hindi"
        "kor", "ko" -> "Korean"
        "nld", "nl" -> "Dutch"
        "pol", "pl" -> "Polish"
        "swe", "sv" -> "Swedish"
        "tur", "tr" -> "Turkish"
        "ukr", "uk" -> "Ukrainian"
        "vie", "vi" -> "Vietnamese"
        "fas", "fa" -> "Persian"
        "heb", "he" -> "Hebrew"
        "mul" -> "Multiple"
        else -> code.uppercase()
    }

    val LANGUAGE_OPTIONS = listOf(
        "all" to "All Languages",
        "eng" to "English",
        "spa" to "Spanish",
        "fra" to "French",
        "deu" to "German",
        "por" to "Portuguese",
        "rus" to "Russian",
        "zho" to "Chinese",
        "ara" to "Arabic",
        "hin" to "Hindi",
        "jpn" to "Japanese"
    )
}
