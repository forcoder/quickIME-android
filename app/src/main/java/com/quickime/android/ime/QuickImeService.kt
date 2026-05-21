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
        return createKeyboardView()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
    }

    override fun onFinishInput() {
        super.onFinishInput()
    }

    private fun createKeyboardView(): View {
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
        val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM⌫", "  空格  ")
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

        return layout
    }

    private fun handleKeyPress(char: Char) {
        val conn = currentInputConnection ?: return

        when (char) {
            '⌫' -> conn.deleteSurroundingText(1, 0)
            ' ' -> conn.commitText(" ", 1)
            else -> conn.commitText(char.toString(), 1)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
