package com.quickime.android.ime

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import com.quickime.core.cs.CustomerServiceManager
import com.quickime.core.wubi.WubiEngine
import com.quickime.ui.theme.QuickIMETheme
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
    private var currentCode = StringBuilder()
    private var isComposing = false

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

    override fun onCreateInputView(): android.view.View {
        return ComposeView(this).apply {
            setContent {
                QuickIMETheme {
                    QuickImeView(
                        context = this@QuickImeService,
                        onKeyListener = { key -> handleKey(key) },
                        onSuggestionListener = { index -> selectSuggestion(index) }
                    )
                }
            }
        }
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        isComposing = false
        currentCode.clear()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        commitCurrentText()
    }

    private fun handleKey(key: KeyEvent) {
        val connection = currentInputConnection ?: return

        when (key.type) {
            KeyType.Character -> {
                if (key.char in 'a'..'z' || key.char in 'A'..'Z') {
                    currentCode.append(key.char.uppercaseChar())
                    isComposing = true
                    updateComposingText(connection)
                }
            }
            KeyType.Backspace -> {
                if (currentCode.isNotEmpty()) {
                    currentCode.deleteCharAt(currentCode.length - 1)
                    if (currentCode.isEmpty()) isComposing = false
                    updateComposingText(connection)
                }
            }
            KeyType.Space -> selectSuggestion(0)
            KeyType.Enter -> {
                commitCurrentText()
                connection.commitText("\n", 1)
            }
            KeyType.SwitchKeyboard -> {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        }
    }

    private fun updateComposingText(connection: android.view.inputmethod.InputConnection) {
        if (isComposing && currentCode.isNotEmpty()) {
            val candidates = wubiEngine.getCandidates()
            val composingText = currentCode.toString() + " " +
                    candidates.take(5).joinToString(" ") { it.text }
            connection.setComposingText(composingText, 1)
        }
    }

    private fun selectSuggestion(index: Int) {
        val connection = currentInputConnection ?: return
        val candidates = wubiEngine.getCandidates()
        if (index in candidates.indices) {
            commitText(candidates[index].text)
            currentCode.clear()
            isComposing = false
        }
    }

    private fun commitCurrentText() {
        if (isComposing && currentCode.isNotEmpty()) {
            val candidates = wubiEngine.getCandidates()
            if (candidates.isNotEmpty()) {
                commitText(candidates[0].text)
            } else {
                commitText(currentCode.toString())
            }
            currentCode.clear()
            isComposing = false
        }
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }
}
