package com.quickime.android.ime.symbol

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView

class SymbolKeyboardView(
    private val ctx: Context,
    private val onSymbolClick: (String) -> Unit,
    private val onBack: () -> Unit
) {
    private var currentCategoryIndex = 0
    private var rootView: LinearLayout? = null
    private var symbolContainer: LinearLayout? = null

    fun createView(): View {
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
            setPadding(dp(8), dp(8), dp(8), dp(8))
        }
        rootView = layout

        // 分类标签栏
        val categoryScroll = HorizontalScrollView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(36)
            )
        }
        val categoryLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        // 显示分类：近期（有符号时）+ 其他分类
        val displayCategories = mutableListOf<Pair<Int, String>>()

        // 如果近期有符号，添加近期分类
        if (SymbolData.recentSymbols.isNotEmpty()) {
            displayCategories.add(Pair(0, "近期"))
        }

        // 添加所有分类
        SymbolData.categories.forEachIndexed { index, category ->
            displayCategories.add(Pair(index + 1, category.name))
        }

        displayCategories.forEachIndexed { displayIndex, (actualIndex, name) ->
            val btn = Button(ctx).apply {
                text = name
                layoutParams = LinearLayout.LayoutParams(
                    dp(64), dp(32)
                ).apply {
                    marginEnd = dp(4)
                }
                setBackgroundColor(
                    if (displayIndex == currentCategoryIndex) 0xFF2196F3.toInt()
                    else 0xFFFFFFFF.toInt()
                )
                setTextColor(
                    if (displayIndex == currentCategoryIndex) 0xFFFFFFFF.toInt()
                    else 0xFF000000.toInt()
                )
                setOnClickListener {
                    switchCategory(displayIndex)
                }
                tag = actualIndex // 保存实际索引
            }
            categoryLayout.addView(btn)
        }
        categoryScroll.addView(categoryLayout)
        layout.addView(categoryScroll)

        // 符号显示区域
        val symbolScroll = ScrollView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        }
        val symbolLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
        }
        symbolContainer = symbolLayout
        symbolScroll.addView(symbolLayout)
        layout.addView(symbolScroll)

        // 底部工具栏
        val toolbar = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(40)
            )
        }

        // 返回按钮
        val backBtn = Button(ctx).apply {
            text = "←返回"
            layoutParams = LinearLayout.LayoutParams(0, dp(36), 1f)
            setOnClickListener { onBack() }
        }
        toolbar.addView(backBtn)

        // 清空按钮
        val clearBtn = Button(ctx).apply {
            text = "清空"
            layoutParams = LinearLayout.LayoutParams(0, dp(36), 1f)
            setOnClickListener {
                // 清空候选
            }
        }
        toolbar.addView(clearBtn)

        layout.addView(toolbar)

        // 初始显示第一类符号
        updateSymbols()

        return layout
    }

    private fun switchCategory(index: Int) {
        currentCategoryIndex = index
        updateSymbols()
        updateCategoryButtons()
    }

    private fun updateSymbols() {
        symbolContainer?.removeAllViews()

        // 获取分类索引（考虑是否有近期分类）
        val hasRecent = SymbolData.recentSymbols.isNotEmpty()
        val actualCategoryIndex = if (hasRecent && currentCategoryIndex == 0) {
            0 // 近期分类
        } else if (hasRecent) {
            currentCategoryIndex // 其他分类需要减1
        } else {
            currentCategoryIndex // 没有近期时不需要调整
        }

        val category = SymbolData.getByIndex(actualCategoryIndex) ?: return
        val symbols = category.symbols.joinToString("")

        // 如果是近期分类且为空，显示提示
        if (symbols.isEmpty() && actualCategoryIndex == 0) {
            val emptyText = TextView(ctx).apply {
                text = "暂无近期符号"
                textSize = 14f
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                setPadding(0, dp(20), 0, 0)
            }
            symbolContainer?.addView(emptyText)
            return
        }

        // 2行布局显示符号
        val lineCount = (symbols.length + 9) / 10

        for (line in 0 until lineCount) {
            val lineLayout = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(44)
                )
            }

            val start = line * 10
            val end = minOf(start + 10, symbols.length)

            for (i in start until end) {
                val btn = Button(ctx).apply {
                    text = symbols[i].toString()
                    layoutParams = LinearLayout.LayoutParams(0, dp(40), 1f)
                    setBackgroundColor(0xFFFFFFFF.toInt())
                    setTextColor(0xFF333333.toInt())
                    textSize = 16f
                    setOnClickListener {
                        val symbol = symbols[i].toString()
                        SymbolData.onSymbolUsed(symbol) // 记录使用
                        onSymbolClick(symbol)
                    }
                }
                lineLayout.addView(btn)
            }

            symbolContainer?.addView(lineLayout)
        }
    }

    private fun updateCategoryButtons() {
        val root = rootView ?: return
        val categoryScroll = root.getChildAt(0) as? HorizontalScrollView ?: return
        val categoryLayout = categoryScroll.getChildAt(0) as? LinearLayout ?: return

        for (i in 0 until categoryLayout.childCount) {
            val btn = categoryLayout.getChildAt(i) as? Button ?: continue
            if (i == currentCategoryIndex) {
                btn.setBackgroundColor(0xFF2196F3.toInt())
                btn.setTextColor(0xFFFFFFFF.toInt())
            } else {
                btn.setBackgroundColor(0xFFFFFFFF.toInt())
                btn.setTextColor(0xFF000000.toInt())
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * ctx.resources.displayMetrics.density).toInt()
    }
}