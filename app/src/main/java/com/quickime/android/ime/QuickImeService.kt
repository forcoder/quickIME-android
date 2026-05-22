package com.quickime.android.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.quickime.core.cs.CustomerServiceManager
import com.quickime.core.pinyin.PinyinEngine
import com.quime.core.wubi.WubiEngine
import com.quickime.android.ime.keyboard.FullKeyboardView
import com.quickime.android.ime.persona.Persona
import com.quickime.android.ime.persona.PersonaDefaults
import com.quickime.android.ime.persona.PersonaManager
import com.quickime.android.ime.persona.PersonaEditActivity
import com.quickime.android.ime.symbol.SymbolKeyboardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class QuickImeService : InputMethodService() {

    @Inject
    lateinit var wubiEngine: WubiEngine

    @Inject
    lateinit var csManager: CustomerServiceManager

    @Inject
    lateinit var pinyinEngine: PinyinEngine

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var currentView: View? = null
    private var isSymbolMode = false
    private var symbolView: SymbolKeyboardView? = null
    private var fullKeyboardView: FullKeyboardView? = null

    private lateinit var personaManager: PersonaManager
    private var currentPersona: Persona = PersonaDefaults.personas.first()

    override fun onCreate() {
        super.onCreate()
        personaManager = PersonaManager(this)
        currentPersona = personaManager.getSelectedPersona()
        scope.launch {
            csManager.initialize()
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onCreateInputView(): View {
        return createMainKeyboardView()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    private fun createMainKeyboardView(): View {
        val kb = FullKeyboardView(
            ctx = this,
            wubiEngine = wubiEngine,
            pinyinEngine = pinyinEngine,
            onTextInput = { text -> commitText(text) },
            onDelete = { deleteChar() },
            onPersonaClick = { showPersonaSelector() },
            currentPersonaName = currentPersona.name,
            currentPersonaIcon = currentPersona.icon
        )
        fullKeyboardView = kb
        currentView = kb.createView()
        return currentView!!
    }

    private fun showPersonaSelector() {
        val personas = personaManager.getAllPersonas()
        val ctx = this

        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        val title = TextView(ctx).apply {
            text = "选择人设"
            textSize = 16f
            setTextColor(0xFF333333.toInt())
            setPadding(0, 0, 0, dp(8))
        }
        container.addView(title)

        val grid = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
        }

        val cols = 2
        var row: LinearLayout? = null

        personas.forEachIndexed { index, persona ->
            if (index % cols == 0) {
                row = LinearLayout(ctx).apply { orientation = LinearLayout.HORIZONTAL }
                grid.addView(row)
            }
            val btn = Button(ctx).apply {
                text = "${persona.icon} ${persona.name}"
                layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                    marginEnd = dp(8)
                    bottomMargin = dp(8)
                }
                textSize = 14f
                gravity = Gravity.CENTER
                if (persona.id == currentPersona.id) {
                    setBackgroundColor(0xFF2196F3.toInt())
                    setTextColor(0xFFFFFFFF.toInt())
                } else {
                    setBackgroundColor(0xFFF5F5F5.toInt())
                    setTextColor(0xFF333333.toInt())
                }
                setOnClickListener {
                    selectPersona(persona)
                    popup?.dismiss()
                }
            }
            row?.addView(btn)
        }
        container.addView(grid)

        val addBtn = Button(ctx).apply {
            text = "➕ 添加自定义人设"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(40)
            ).apply { topMargin = dp(8) }
            textSize = 14f
            setBackgroundColor(0xFFE8F5E9.toInt())
            setTextColor(0xFF2E7D32.toInt())
            setOnClickListener {
                val intent = Intent(ctx, PersonaEditActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                popup?.dismiss()
            }
        }
        container.addView(addBtn)

        val p = PopupWindow(
            container,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 8f
            isFocusable = true
        }
        popup = p
        p.showAtLocation(window?.peekDecorView() ?: findViewById(android.R.id.content), Gravity.BOTTOM, 0, dp(56))
    }

    private var popup: PopupWindow? = null

    private fun selectPersona(persona: Persona) {
        currentPersona = persona
        personaManager.setSelectedPersona(persona)
        setInputView(createMainKeyboardView())
    }

    private fun createSymbolKeyboardView(): View {
        val kb = SymbolKeyboardView(
            ctx = this,
            onSymbolClick = { symbol ->
                val conn = currentInputConnection ?: return@SymbolKeyboardView
                conn.commitText(symbol, 1)
            },
            onBack = { switchToMainKeyboard() }
        )
        symbolView = kb
        isSymbolMode = true
        return kb.createView()
    }

    private fun switchToSymbolKeyboard() {
        setInputView(createSymbolKeyboardView())
    }

    private fun switchToMainKeyboard() {
        isSymbolMode = false
        symbolView = null
        fullKeyboardView = null
        setInputView(createMainKeyboardView())
    }

    private fun commitText(text: String) {
        val conn = currentInputConnection ?: return
        conn.commitText(text, 1)
    }

    private fun deleteChar() {
        val conn = currentInputConnection ?: return
        conn.deleteSurroundingText(1, 0)
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
