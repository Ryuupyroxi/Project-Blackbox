package com.blackbox.module.kai.data

import kotlinx.serialization.Serializable

@Serializable
data class Service(
    val id: String,
    val name: String,
    val description: String = ""
) {
    @Serializable
    data class AIHorde(val apiKey: String = "") : Service("aihorde", "AI Horde")
    @Serializable
    data class AiHubMix(val apiKey: String = "") : Service("aihubmix", "AI Hub Mix")
    @Serializable
    data class Anthropic(val apiKey: String = "") : Service("anthropic", "Anthropic")
    @Serializable
    data class AtlasCloud(val apiKey: String = "") : Service("atlascloud", "Atlas Cloud")
    @Serializable
    data class Cerebras(val apiKey: String = "") : Service("cerebras", "Cerebras")
    @Serializable
    data class DeepInfra(val apiKey: String = "") : Service("deepinfra", "DeepInfra")
    @Serializable
    data class DeepSeek(val apiKey: String = "") : Service("deepseek", "DeepSeek")
    @Serializable
    data class FireworksAI(val apiKey: String = "") : Service("fireworksai", "Fireworks AI")
    @Serializable
    data class Free : Service("free", "Free")
    @Serializable
    data class Gemini(val apiKey: String = "") : Service("gemini", "Gemini")
    @Serializable
    data class Groq(val apiKey: String = "") : Service("groq", "Groq")
    @Serializable
    data class HuggingFace(val apiKey: String = "") : Service("huggingface", "Hugging Face")
    @Serializable
    data class LiteRT : Service("litert", "LiteRT")
    @Serializable
    data class LongCat(val apiKey: String = "") : Service("longcat", "LongCat")
    @Serializable
    data class Minimax(val apiKey: String = "") : Service("minimax", "Minimax")
    @Serializable
    data class Mistral(val apiKey: String = "") : Service("mistral", "Mistral")
    @Serializable
    data class Moonshot(val apiKey: String = "") : Service("moonshot", "Moonshot")
    @Serializable
    data class Nvidia(val apiKey: String = "") : Service("nvidia", "NVIDIA")
    @Serializable
    data class OllamaCloud(val endpoint: String = "") : Service("ollamacloud", "Ollama Cloud")
    @Serializable
    data class OpenAI(val apiKey: String = "") : Service("openai", "OpenAI")
    @Serializable
    data class OpenAICompatible(val endpoint: String = "", val apiKey: String = "") : Service("openai_compatible", "OpenAI Compatible")
    @Serializable
    data class OpenCode(val apiKey: String = "") : Service("openode", "OpenCode")
    @Serializable
    data class OpenRouter(val apiKey: String = "") : Service("openrouter", "OpenRouter")
    @Serializable
    data class Perplexity(val apiKey: String = "") : Service("perplexity", "Perplexity")
    @Serializable
    data class PublicAI(val apiKey: String = "") : Service("publicai", "PublicAI")
    @Serializable
    data class Together(val apiKey: String = "") : Service("together", "Together")
    @Serializable
    data class Venice(val apiKey: String = "") : Service("venice", "Venice")
    @Serializable
    data class XAI(val apiKey: String = "") : Service("xai", "xAI")
    @Serializable
    data class ZaiCodingPlan(val apiKey: String = "") : Service("zai_coding_plan", "Zai Coding Plan")
    @Serializable
    data class Zai(val apiKey: String = "") : Service("zai", "Zai")

    companion object {
        val entries: List<Service> = listOf(
            AIHorde(), AiHubMix(), Anthropic(), AtlasCloud(), Cerebras(),
            DeepInfra(), DeepSeek(), FireworksAI(), Free(), Gemini(),
            Groq(), HuggingFace(), LiteRT(), LongCat(), Minimax(),
            Mistral(), Moonshot(), Nvidia(), OllamaCloud(), OpenAI(),
            OpenAICompatible(), OpenCode(), OpenRouter(), Perplexity(),
            PublicAI(), Together(), Venice(), XAI(), ZaiCodingPlan(), Zai()
        )
    }
}
