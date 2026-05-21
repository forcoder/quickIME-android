package com.quickime.android.ime.persona

data class Persona(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val systemPrompt: String,
    val temperature: Float = 0.7f,
    val isCustom: Boolean = false
) {
    companion object {
        fun fromJson(json: Map<String, Any>): Persona {
            return Persona(
                id = json["id"] as? String ?: "",
                name = json["name"] as? String ?: "",
                icon = json["icon"] as? String ?: "",
                description = json["description"] as? String ?: "",
                systemPrompt = json["systemPrompt"] as? String ?: "",
                temperature = (json["temperature"] as? Number)?.toFloat() ?: 0.7f,
                isCustom = json["isCustom"] as? Boolean ?: false
            )
        }
    }

    fun toJson(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "icon" to icon,
        "description" to description,
        "systemPrompt" to systemPrompt,
        "temperature" to temperature,
        "isCustom" to isCustom
    )
}