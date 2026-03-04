package com.example.sentinelai

object SecurityAdvisor {

    fun explain(permission: String): String {

        return when {

            permission.contains("LOCATION") ->
                "Location access may allow the app to track your movement."

            permission.contains("CONTACT") ->
                "Contacts access may expose your personal network."

            permission.contains("CAMERA") ->
                "Camera permission allows the app to capture photos or videos."

            permission.contains("AUDIO") ->
                "Microphone access allows recording audio."

            permission.contains("SMS") ->
                "SMS access could allow reading your messages."

            permission.contains("STORAGE") ->
                "Storage access may allow reading files from your device."

            else ->
                "This permission may access sensitive device resources."
        }
    }
}