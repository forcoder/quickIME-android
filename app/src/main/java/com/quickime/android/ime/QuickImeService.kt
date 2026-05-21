package com.quickime.android.ime

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import com.quickime.core.cs.CustomerServiceManager
import com.quickime.core.pinyin.PinyinEngine
import com.quickime.core.wubi.WubiEngine
import com.quickime.android.ime.keyboard.FullKeyboardView
import com.quickime.android.ime.keyboard.InputMode
import com.quickime.android.ime.persona.Persona
import com.quickime.android.ime.persona.PersonaDefaults
import com.quickime.android.ime.persona.PersonaManager
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
        fullKeyboardView = FullKeyboardView(
            ctx = this,
            wubiEngine = wubiEngine,
            pinyinEngine = pinyinEngine,
            onTextInput = { text -> commitText(text) },
            onDelete = { deleteChar() },
            onPersonaClick = { showPersonaSelector(findViewById(android.R.id.content)) },
            currentPersonaName = currentPersona.name,
            currentPersonaIcon = currentPersona.icon
        )

        currentView = fullKeyboardView?.createView()
        return currentView!!
    }

    private fun showPersonaSelector(anchor: View) {
        val personas = personaManager.getAllPersonas()

        val popupView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        // 标题
        val title = TextView(this).apply {
            text = "选择人设"
            textSize = 16f
            setTextColor(0xFF333333.toInt())
            setPadding(0, 0, 0, dp(8))
        }
        popupView.addView(title)

        // 人设网格
        val gridContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val cols = 2
        var currentRow: LinearLayout? = null

        personas.forEachIndexed { index, persona ->
            if (index % cols == 0) {
                currentRow = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                gridContainer.addView(currentRow)
            }

            val btn = Button(this).apply {
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
                    popup.dismiss()
                }
            }
            currentRow?.addView(btn)
        }

        // 自定义按钮
        val customBtn = Button(this).apply {
            text = "➕ 添加自定义人设"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(40)
            ).apply {
                topMargin = dp(8)
            }
            textSize = 14f
            setBackgroundColor(0xFFE8F5E9.toInt())
            setTextColor(0xFF2E7D32.toInt())
            setOnClickListener {
                // 打开自定义人设编辑页面
                val intent = Intent(this@QuickImeService, PersonaEditActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                popup.dismiss()
            }
        }
        popupView.addView(customBtn)
        popupView.addView(gridContainer)

        val popup = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 8f
            isFocusable = true
        }

        popup.showAtLocation(anchor, Gravity.BOTTOM, 0, dp(56))
    }

    private fun selectPersona(persona: Persona) {
        currentPersona = persona
        personaManager.setSelectedPersona(persona)
        // 刷新键盘UI
        setInputView(createMainKeyboardView())
    }

    private fun createSymbolKeyboardView(): View {
        val keyboard = SymbolKeyboardView(
            ctx = this,
            onSymbolClick = { symbol ->
                val conn = currentInputConnection ?: return@SymbolKeyboardView
                conn.commitText(symbol, 1)
            },
            onBack = {
                switchToMainKeyboard()
            }
        )
        symbolView = keyboard
        isSymbolMode = true
        return keyboard.createView()
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
