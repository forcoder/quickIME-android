package com.quickime.android.ime.persona

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PersonaManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("persona_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_CUSTOM_PERSONAS = "custom_personas"
        private const val KEY_SELECTED_PERSONA = "selected_persona"
        private const val DEFAULT_PERSONA = "beauty_girl"
    }

    fun getAllPersonas(): List<Persona> {
        return PersonaDefaults.personas + getCustomPersonas()
    }

    fun getPersonaById(id: String): Persona? {
        return getAllPersonas().find { it.id == id }
    }

    fun getSelectedPersona(): Persona {
        val id = prefs.getString(KEY_SELECTED_PERSONA, DEFAULT_PERSONA) ?: DEFAULT_PERSONA
        return getPersonaById(id) ?: PersonaDefaults.personas.first()
    }

    fun setSelectedPersona(persona: Persona) {
        prefs.edit().putString(KEY_SELECTED_PERSONA, persona.id).apply()
    }

    private fun getCustomPersonas(): List<Persona> {
        val json = prefs.getString(KEY_CUSTOM_PERSONAS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Persona>>() {}.type
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addCustomPersona(persona: Persona) {
        val custom = getCustomPersonas().toMutableList()
        val newPersona = persona.copy(id = "custom_${System.currentTimeMillis()}", isCustom = true)
        custom.add(newPersona)
        saveCustomPersonas(custom)
    }

    fun updateCustomPersona(persona: Persona) {
        if (!persona.isCustom) return
        val custom = getCustomPersonas().toMutableList()
        val index = custom.indexOfFirst { it.id == persona.id }
        if (index >= 0) {
            custom[index] = persona
            saveCustomPersonas(custom)
        }
    }

    fun deleteCustomPersona(personaId: String) {
        if (!personaId.startsWith("custom_")) return
        val custom = getCustomPersonas().toMutableList()
        custom.removeAll { it.id == personaId }
        saveCustomPersonas(custom)
    }

    private fun saveCustomPersonas(personas: List<Persona>) {
        val json = gson.toJson(personas)
        prefs.edit().putString(KEY_CUSTOM_PERSONAS, json).apply()
    }

    fun resetToDefault() {
        prefs.edit().putString(KEY_SELECTED_PERSONA, DEFAULT_PERSONA).apply()
    }

    fun buildAiPrompt(userMessage: String): String {
        val persona = getSelectedPersona()
        return """
            |${persona.systemPrompt}
            |
            |用户消息: $userMessage
            |
            |请以${persona.name}的风格回复用户。
        """.trimMargin()
    }
}