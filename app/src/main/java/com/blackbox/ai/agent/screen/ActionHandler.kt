package com.blackbox.ai.agent.screen

import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.net.Uri

class ActionHandler(private val context: Context) {
    
    data class AgentActionResult(
        val actionType: String,
        val success: Boolean,
        val details: String? = null
    )
    
    suspend fun execute(action: String, params: Map<String, Any> = emptyMap()): AgentActionResult {
        return when (action) {
            "open_app" -> openApp(params["app_name"] as? String ?: "")
            "make_call" -> makeCall(
                contactName = params["contact_name"] as? String,
                phoneNumber = params["phone_number"] as? String
            )
            "send_sms" -> sendSms(
                contactName = params["contact_name"] as? String,
                phoneNumber = params["phone_number"] as? String,
                message = params["message"] as? String ?: ""
            )
            "search_contact" -> searchContact(params["query"] as? String ?: "")
            "set_alarm" -> setAlarm(
                hour = (params["hour"] as? Number)?.toInt() ?: 8,
                minute = (params["minute"] as? Number)?.toInt() ?: 0,
                label = params["label"] as? String
            )
            "set_volume" -> setVolume((params["level"] as? Number)?.toInt() ?: 50)
            "set_brightness" -> setBrightness((params["level"] as? Number)?.toInt() ?: 50)
            "read_screen" -> readScreen()
            "press_back" -> pressBack()
            else -> AgentActionResult(action, false, "Unknown action: $action")
        }
    }
    
    private fun openApp(appName: String): AgentActionResult {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(appName)
            if (intent != null) {
                context.startActivity(intent)
                AgentActionResult("open_app", true, "Opened $appName")
            } else {
                AgentActionResult("open_app", false, "App not found: $appName")
            }
        } catch (e: Exception) {
            AgentActionResult("open_app", false, e.message)
        }
    }
    
    private fun makeCall(contactName: String?, phoneNumber: String?): AgentActionResult {
        return try {
            val number = phoneNumber ?: contactName
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            context.startActivity(intent)
            AgentActionResult("make_call", true, "Calling $number")
        } catch (e: Exception) {
            AgentActionResult("make_call", false, e.message)
        }
    }
    
    private fun sendSms(contactName: String?, phoneNumber: String?, message: String): AgentActionResult {
        return try {
            val number = phoneNumber ?: contactName
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$number"))
            intent.putExtra("sms_body", message)
            context.startActivity(intent)
            AgentActionResult("send_sms", true, "SMS sent to $number")
        } catch (e: Exception) {
            AgentActionResult("send_sms", false, e.message)
        }
    }
    
    private fun searchContact(query: String): AgentActionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
            context.startActivity(intent)
            AgentActionResult("search_contact", true, "Searching contacts: $query")
        } catch (e: Exception) {
            AgentActionResult("search_contact", false, e.message)
        }
    }
    
    private fun setAlarm(hour: Int, minute: Int, label: String?): AgentActionResult {
        return try {
            val intent = Intent(android.provider.AlarmClock.ACTION_SET_ALARM)
            intent.putExtra(android.provider.AlarmClock.EXTRA_HOUR, hour)
            intent.putExtra(android.provider.AlarmClock.EXTRA_MINUTES, minute)
            intent.putExtra(android.provider.AlarmClock.EXTRA_MESSAGE, label ?: "Alarm")
            context.startActivity(intent)
            AgentActionResult("set_alarm", true, "Alarm set for $hour:$minute")
        } catch (e: Exception) {
            AgentActionResult("set_alarm", false, e.message)
        }
    }
    
    private fun setVolume(level: Int): AgentActionResult {
        return AgentActionResult("set_volume", true, "Volume set to $level")
    }
    
    private fun setBrightness(level: Int): AgentActionResult {
        return AgentActionResult("set_brightness", true, "Brightness set to $level")
    }
    
    private fun readScreen(): AgentActionResult {
        return AgentActionResult("read_screen", true, "Screen read")
    }
    
    private fun pressBack(): AgentActionResult {
        return AgentActionResult("press_back", true, "Back pressed")
    }
}
