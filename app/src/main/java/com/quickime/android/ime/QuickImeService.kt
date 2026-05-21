package com.quickime.android.ime

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import com.quickime.core.cs.CustomerServiceManager
import com.quickime.core.wubi.WubiEngine
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

    override fun onCreate() {
        super.onCreate()
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

        // 候选栏
        val candidateScroll = HorizontalScrollView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(40)
            )
        }
        val candidateLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
        }
        candidateScroll.addView(candidateLayout)
        layout.addView(candidateScroll)

        // 键盘行
        val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM⌫", "🌐符号空格")
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

    private fun createSymbolKeyboardView(): View {
        val keyboard = SymbolKeyboardView(
            ctx = this,
            onSymbolClick = { symbol ->
                val conn = currentInputConnection ?: return@SymbolKeyboardView
                conn.commitText(symbol, 1)
            },
            onBack = {
                // 切换回主键盘
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
            '🌐' -> switchToSymbolKeyboard()  // 切换到符号键盘
            else -> conn.commitText(char.toString(), 1)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
