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
import com.quickime.core.wubi.WubiEngine
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

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var currentView: View? = null
    private var isSymbolMode = false
    private var symbolView: SymbolKeyboardView? = null

    private lateinit var personaManager: PersonaManager
    private var currentPersona: Persona = PersonaDefaults.personas.first()

    override fun onCreate() {
        super.onCreate()
        personaManager = PersonaManager(this)
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
        val ctx = this
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }

        // 人设 + 候选栏区域
        val topBar = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(40)
            )
        }

        // 人设切换按钮
        val personaBtn = Button(ctx).apply {
            text = "${currentPersona.icon} ${currentPersona.name}"
            layoutParams = LinearLayout.LayoutParams(dp(100), dp(36))
            textSize = 12f
            setBackgroundColor(0xFF2196F3.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { showPersonaSelector(it) }
        }
        topBar.addView(personaBtn)

        // 候选栏
        val candidateScroll = HorizontalScrollView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(36), 1f)
        }
        val candidateLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        candidateScroll.addView(candidateLayout)
        topBar.addView(candidateScroll)

        layout.addView(topBar)

        // 键盘行
        val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM⌫", "🌐符号 空格")
        rows.forEach { row ->
            val rowLayout = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(48)
                ).apply {
                    topMargin = dp(4)
                }
            }
            row.forEach { char ->
                val btn = Button(ctx).apply {
                    text = char.toString()
                    layoutParams = LinearLayout.LayoutParams(0, dp(44), 1f)
                    setBackgroundColor(0xFFFFFFFF.toInt())
                    setTextColor(0xFF000000.toInt())
                    setOnClickListener { handleKeyPress(char) }
                }
                rowLayout.addView(btn)
            }
            layout.addView(rowLayout)
        }

        currentView = layout
        return layout
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

        popup.showAtLocation(anchor, Gravity.BOTTOM, 0, anchor.height + dp(8))
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
        setInputView(createMainKeyboardView())
    }

    private fun handleKeyPress(char: Char) {
        val conn = currentInputConnection ?: return

        when (char) {
            '⌫' -> conn.deleteSurroundingText(1, 0)
            ' ' -> conn.commitText(" ", 1)
            '🌐' -> switchToSymbolKeyboard()
            else -> conn.commitText(char.toString(), 1)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
