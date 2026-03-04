package com.example.sentinelai

object PermissionFormatter {

    fun format(permission: String): String {

        return when {

            permission.contains("CAMERA") -> "Camera Access"
            permission.contains("LOCATION") -> "Location Access"
            permission.contains("CONTACT") -> "Contacts Access"
            permission.contains("SMS") -> "SMS Access"
            permission.contains("AUDIO") -> "Microphone Access"
            permission.contains("STORAGE") -> "Storage Access"

            else -> "Sensitive System Access"
        }
    }
}