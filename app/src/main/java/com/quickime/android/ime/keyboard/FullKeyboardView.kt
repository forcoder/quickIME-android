package com.quickime.android.ime.keyboard

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import com.quickime.core.pinyin.PinyinEngine
import com.quickime.core.pinyin.PinyinSession
import com.quickime.core.wubi.WubiEngine

/**
 * 完整键盘视图
 * 支持五笔、拼音混合输入
 */
class FullKeyboardView(
    private val ctx: Context,
    private val wubiEngine: WubiEngine,
    private val pinyinEngine: PinyinEngine,
    private val onTextInput: (String) -> Unit,
    private val onDelete: () -> Unit,
    private val onPersonaClick: () -> Unit,
    private val currentPersonaName: String,
    private val currentPersonaIcon: String
) {
    private var state = KeyboardState()
    private var rootView: View? = null

    /**
     * 创建键盘视图
     */
    fun createView(): View {
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        rootView = layout

        // 顶部栏：人设按钮 + 模式切换 + 候选栏
        layout.addView(createTopBar())

        // 键盘区域
        layout.addView(createKeyboardArea())

        // 底部工具栏
        layout.addView(createBottomToolbar())

        return layout
    }

    /**
     * 创建顶部栏
     */
    private fun createTopBar(): View {
        val bar = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(40)
            )
        }

        // 人设按钮
        val personaBtn = Button(ctx).apply {
            text = "$currentPersonaIcon $currentPersonaName"
            layoutParams = LinearLayout.LayoutParams(dp(90), dp(36))
            textSize = 11f
            setBackgroundColor(0xFF2196F3.toInt())
            setTextColor(Color.WHITE)
            setOnClickListener { onPersonaClick() }
        }
        bar.addView(personaBtn)

        // 模式切换按钮
        val modeBtn = Button(ctx).apply {
            text = when (state.inputMode) {
                InputMode.WuBi -> "五笔"
                InputMode.Pinyin -> "拼音"
                InputMode.Symbol -> "符号"
            }
            layoutParams = LinearLayout.LayoutParams(dp(50), dp(36))
            textSize = 11f
            setBackgroundColor(0xFFE8E8E8.toInt())
            setTextColor(Color.BLACK)
            setOnClickListener { switchMode() }
        }
        bar.addView(modeBtn)

        // 候选栏
        val candidateScroll = HorizontalScrollView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(36), 1f)
        }
        val candidateLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // 显示输入内容（拼音或五笔编码）
        if (state.currentInput.isNotEmpty()) {
            val inputText = TextView(ctx).apply {
                text = state.currentInput
                textSize = 16f
                setTextColor(Color.BLACK)
                setPadding(dp(8), 0, dp(8), 0)
            }
            candidateLayout.addView(inputText)
        }

        // 候选词
        state.currentPageCandidates.forEachIndexed { index, candidate ->
            val btn = Button(ctx).apply {
                text = candidate.text
                layoutParams = LinearLayout.LayoutParams(dp(48), dp(32))
                textSize = 14f

                // 显示序号
                val numberBg = if (index < 9) "${index + 1} " else ""
                text = "$numberBg${candidate.text}"

                setBackgroundColor(Color.WHITE)
                setTextColor(Color.BLACK)

                // 标记不同类型的候选
                when (candidate.type) {
                    CandidateType.Knowledge -> setTextColor(Color.parseColor("#4CAF50"))
                    CandidateType.AI -> setTextColor(Color.parseColor("#2196F3"))
                    CandidateType.Pinyin -> setTextColor(Color.parseColor("#9E9E9E"))
                    else -> {}
                }

                setOnClickListener { selectCandidate(index) }
            }
            candidateLayout.addView(btn)
        }

        // 翻页指示
        if (state.totalPages > 1) {
            val pageText = TextView(ctx).apply {
                text = "${state.pageIndex + 1}/${state.totalPages}"
                textSize = 12f
                setTextColor(Color.GRAY)
                setPadding(dp(4), 0, dp(4), 0)
            }
            candidateLayout.addView(pageText)
        }

        candidateScroll.addView(candidateLayout)
        bar.addView(candidateScroll)

        return bar
    }

    /**
     * 创建键盘区域
     */
    private fun createKeyboardArea(): View {
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
        }

        // 字母行
        val rows = when (state.inputMode) {
            InputMode.WuBi, InputMode.Pinyin -> KeyboardLayout.qwertyRows
            InputMode.Symbol -> listOf(
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
                listOf("-", "/", ":", ";", "(", ")", "$", "&", "@", "\""),
                listOf(".", ",", "?", "!", "'")
            )
        }

        for (row in rows) {
            val rowLayout = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(46)
                ).apply {
                    topMargin = dp(3)
                }
            }

            for (key in row) {
                val displayText = when (state.inputMode) {
                    InputMode.Symbol -> key  // 符号模式直接显示
                    else -> key.uppercase()  // 字母模式大写显示
                }
                val btn = Button(ctx).apply {
                    text = displayText
                    layoutParams = LinearLayout.LayoutParams(0, dp(42), 1f)
                    setBackgroundColor(Color.WHITE)
                    setTextColor(Color.BLACK)
                    textSize = 16f
                    setOnClickListener { handleKeyClick(key) }
                }
                rowLayout.addView(btn)
            }

            container.addView(rowLayout)
        }

        return container
    }

    /**
     * 创建底部工具栏
     */
    private fun createBottomToolbar(): View {
        val toolbar = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(44)
            ).apply {
                topMargin = dp(4)
            }
        }

        // 切换到符号键盘
        val symbolBtn = Button(ctx).apply {
            text = "🌐符号"
            layoutParams = LinearLayout.LayoutParams(dp(70), dp(40))
            textSize = 12f
            setBackgroundColor(0xFFE8E8E8.toInt())
            setTextColor(Color.BLACK)
            setOnClickListener { switchToSymbolMode() }
        }
        toolbar.addView(symbolBtn)

        // 空格键
        val spaceBtn = Button(ctx).apply {
            text = "空格"
            layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f)
            textSize = 14f
            setBackgroundColor(Color.WHITE)
            setTextColor(Color.BLACK)
            setOnClickListener { handleSpace() }
        }
        toolbar.addView(spaceBtn)

        // 翻页按钮
        if (state.candidates.isNotEmpty()) {
            val prevBtn = Button(ctx).apply {
                text = "◀"
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                textSize = 14f
                setBackgroundColor(0xFFE8E8E8.toInt())
                setTextColor(Color.BLACK)
                isEnabled = state.hasPrevPage
                alpha = if (state.hasPrevPage) 1f else 0.3f
                setOnClickListener { prevPage() }
            }
            toolbar.addView(prevBtn)

            val nextBtn = Button(ctx).apply {
                text = "▶"
                layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
                textSize = 14f
                setBackgroundColor(0xFFE8E8E8.toInt())
                setTextColor(Color.BLACK)
                isEnabled = state.hasNextPage
                alpha = if (state.hasNextPage) 1f else 0.3f
                setOnClickListener { nextPage() }
            }
            toolbar.addView(nextBtn)
        }

        // 删除键
        val deleteBtn = Button(ctx).apply {
            text = "⌫"
            layoutParams = LinearLayout.LayoutParams(dp(60), dp(40))
            textSize = 16f
            setBackgroundColor(0xFFE8E8E8.toInt())
            setTextColor(Color.BLACK)
            setOnClickListener { handleDelete() }
            setOnLongClickListener {
                handleClear()
                true
            }
        }
        toolbar.addView(deleteBtn)

        // 回车键
        val enterBtn = Button(ctx).apply {
            text = "换行"
            layoutParams = LinearLayout.LayoutParams(dp(50), dp(40))
            textSize = 12f
            setBackgroundColor(0xFFE8E8E8.toInt())
            setTextColor(Color.BLACK)
            setOnClickListener { handleEnter() }
        }
        toolbar.addView(enterBtn)

        return toolbar
    }

    /**
     * 处理按键点击
     */
    private fun handleKeyClick(key: String) {
        when (state.inputMode) {
            InputMode.WuBi -> handleWubiInput(key)
            InputMode.Pinyin -> handlePinyinInput(key)
            InputMode.Symbol -> handleSymbolInput(key)
        }
        refreshView()
    }

    /**
     * 处理五笔输入
     */
    private fun handleWubiInput(key: String) {
        val newInput = state.currentInput + key
        state = state.copy(currentInput = newInput)

        // 使用WubiEngine查询候选（需要传入编码）
        val candidates = wubiEngine.query(newInput)
        if (candidates.isNotEmpty()) {
            state = state.copy(
                candidates = candidates.map { Candidate(it.text, CandidateType.Normal) },
                pageIndex = 0,
                selectedIndex = 0
            )
        }
    }

    /**
     * 处理拼音输入
     */
    private fun handlePinyinInput(key: String) {
        // 过滤掉非字母字符
        val filteredKey = key.lowercase().filter { it in 'a'..'z' }
        if (filteredKey.isEmpty()) return

        pinyinEngine.inputChar(filteredKey.first())
        val session = pinyinEngine.getSession()

        state = state.copy(
            currentInput = session.pinyin,
            candidates = session.currentPageCandidates.map {
                Candidate(it.text, CandidateType.Pinyin, it.fullPinyin)
            },
            pageIndex = session.pageIndex
        )
    }

    /**
     * 处理符号输入
     */
    private fun handleSymbolInput(key: String) {
        onTextInput(key)
    }

    /**
     * 处理空格
     */
    private fun handleSpace() {
        if (state.candidates.isNotEmpty()) {
            // 选择第一个候选词
            selectCandidate(0)
        } else {
            // 输入空格
            onTextInput(" ")
            clearInput()
        }
    }

    /**
     * 处理删除
     */
    private fun handleDelete() {
        if (state.currentInput.isNotEmpty()) {
            if (state.inputMode == InputMode.Pinyin) {
                pinyinEngine.deleteChar()
                val session = pinyinEngine.getSession()
                state = state.copy(
                    currentInput = session.pinyin,
                    candidates = session.currentPageCandidates.map {
                        Candidate(it.text, CandidateType.Pinyin, it.fullPinyin)
                    },
                    pageIndex = session.pageIndex
                )
            } else {
                state = state.copy(currentInput = state.currentInput.dropLast(1))

                // 更新候选
                if (state.currentInput.isNotEmpty()) {
                    val candidates = wubiEngine.query(state.currentInput)
                    state = state.copy(candidates = candidates.map { Candidate(it.text) })
                } else {
                    state = state.copy(candidates = emptyList())
                }
            }
        } else {
            onDelete()
        }
    }

    /**
     * 清空输入
     */
    private fun handleClear() {
        clearInput()
        if (state.inputMode == InputMode.Pinyin) {
            pinyinEngine.clear()
        }
    }

    /**
     * 清空当前输入
     */
    private fun clearInput() {
        state = state.copy(
            currentInput = "",
            candidates = emptyList(),
            pageIndex = 0,
            selectedIndex = 0
        )
    }

    /**
     * 处理回车
     */
    private fun handleEnter() {
        if (state.currentInput.isNotEmpty()) {
            // 先输入编码本身
            onTextInput(state.currentInput)
            clearInput()
        }
        onTextInput("\n")
    }

    /**
     * 选择候选词
     */
    private fun selectCandidate(index: Int) {
        if (index < 0 || index >= state.currentPageCandidates.size) return

        val candidate = state.currentPageCandidates[index]
        onTextInput(candidate.text)
        clearInput()
    }

    /**
     * 上一页
     */
    private fun prevPage() {
        if (state.inputMode == InputMode.Pinyin) {
            pinyinEngine.prevPage()
            val session = pinyinEngine.getSession()
            state = state.copy(
                pageIndex = session.pageIndex,
                candidates = session.currentPageCandidates.map {
                    Candidate(it.text, CandidateType.Pinyin, it.fullPinyin)
                }
            )
        } else {
            state = state.copy(pageIndex = state.pageIndex - 1)
        }
    }

    /**
     * 下一页
     */
    private fun nextPage() {
        if (state.inputMode == InputMode.Pinyin) {
            pinyinEngine.nextPage()
            val session = pinyinEngine.getSession()
            state = state.copy(
                pageIndex = session.pageIndex,
                candidates = session.currentPageCandidates.map {
                    Candidate(it.text, CandidateType.Pinyin, it.fullPinyin)
                }
            )
        } else {
            state = state.copy(pageIndex = state.pageIndex + 1)
        }
    }

    /**
     * 切换模式
     */
    private fun switchMode() {
        state = when (state.inputMode) {
            InputMode.WuBi -> state.copy(inputMode = InputMode.Pinyin, currentInput = "", candidates = emptyList())
            InputMode.Pinyin -> state.copy(inputMode = InputMode.Symbol, currentInput = "", candidates = emptyList())
            InputMode.Symbol -> state.copy(inputMode = InputMode.WuBi, currentInput = "", candidates = emptyList())
        }
        pinyinEngine.clear()
    }

    /**
     * 切换到符号模式
     */
    private fun switchToSymbolMode() {
        state = state.copy(inputMode = InputMode.Symbol, currentInput = "", candidates = emptyList())
    }

    /**
     * 刷新视图
     */
    private fun refreshView() {
        rootView?.let { view ->
            val parent = view.parent as? android.view.ViewGroup ?: return
            val index = parent.indexOfChild(view)
            parent.removeView(view)

            val newView = createView()
            parent.addView(newView, index)
        }
    }

    /**
     * 获取当前状态
     */
    fun getState(): KeyboardState = state

    /**
     * 设置状态
     */
    fun setState(newState: KeyboardState) {
        state = newState
    }

    private fun dp(value: Int): Int {
        return (value * ctx.resources.displayMetrics.density).toInt()
    }
}