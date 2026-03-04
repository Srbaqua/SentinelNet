package com.example.sentinelai

object MitigationAdvisor {

    fun getRecommendations(permissions: List<String>): List<String> {

        val advice = mutableListOf<String>()

        permissions.forEach { perm ->

            when {

                perm.contains("CAMERA") ->
                    advice.add("Disable Camera permission if the app does not require it.")

                perm.contains("LOCATION") ->
                    advice.add("Set Location access to 'While using the app' instead of 'Always'.")

                perm.contains("CONTACT") ->
                    advice.add("Review Contacts permission in device settings.")

                perm.contains("SMS") ->
                    advice.add("Avoid giving SMS access unless absolutely necessary.")

                perm.contains("AUDIO") ->
                    advice.add("Restrict Microphone access when not in use.")

                perm.contains("STORAGE") ->
                    advice.add("Limit storage access to prevent file exposure.")
            }
        }

        return advice.distinct()
    }
}