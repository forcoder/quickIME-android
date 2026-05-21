package com.quickime.android.ime.persona

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quickime.ui.theme.QuickIMETheme

class PersonaEditActivity : ComponentActivity() {

    private lateinit var personaManager: PersonaManager
    private var editingPersona: Persona? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        personaManager = PersonaManager(this)

        val personaId = intent.getStringExtra("persona_id")
        editingPersona = personaId?.let { personaManager.getPersonaById(it) }

        setContent {
            QuickIMETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PersonaEditScreen(
                        persona = editingPersona,
                        onSave = { persona ->
                            if (persona.isCustom) {
                                personaManager.updateCustomPersona(persona)
                            } else {
                                personaManager.addCustomPersona(persona)
                            }
                            finish()
                        },
                        onDelete = { persona ->
                            personaManager.deleteCustomPersona(persona.id)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonaEditScreen(
    persona: Persona?,
    onSave: (Persona) -> Unit,
    onDelete: (Persona) -> Unit
) {
    var name by remember { mutableStateOf(persona?.name ?: "") }
    var icon by remember { mutableStateOf(persona?.icon ?: "👤") }
    var description by remember { mutableStateOf(persona?.description ?: "") }
    var systemPrompt by remember { mutableStateOf(persona?.systemPrompt ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = if (persona != null && persona.isCustom) "编辑自定义人设" else "创建自定义人设",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = icon,
            onValueChange = { icon = it },
            label = { Text("图标（emoji）") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("名称") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("描述") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = systemPrompt,
            onValueChange = { systemPrompt = it },
            label = { Text("人设提示词（AI 会根据这个生成回复）") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            maxLines = 10
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && systemPrompt.isNotBlank()) {
                    val newPersona = Persona(
                        id = persona?.id ?: "custom_${System.currentTimeMillis()}",
                        name = name,
                        icon = icon.ifBlank { "👤" },
                        description = description,
                        systemPrompt = systemPrompt,
                        isCustom = true
                    )
                    onSave(newPersona)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = name.isNotBlank() && systemPrompt.isNotBlank()
        ) {
            Text(if (persona != null && persona.isCustom) "保存修改" else "创建人设")
        }

        if (persona != null && persona.isCustom) {
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onDelete(persona) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("删除人设")
            }
        }
    }
}