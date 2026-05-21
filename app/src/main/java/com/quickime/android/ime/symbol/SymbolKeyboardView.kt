package com.quickime.android.ime.symbol

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.quickime.core.wubi.WubiEngine

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

        val categories = SymbolData.categories
        categories.forEachIndexed { index, category ->
            val btn = Button(ctx).apply {
                text = category.name
                layoutParams = LinearLayout.LayoutParams(
                    dp(64), dp(32)
                ).apply {
                    marginEnd = dp(4)
                }
                setBackgroundColor(
                    if (index == currentCategoryIndex) 0xFF2196F3.toInt()
                    else 0xFFFFFFFF.toInt()
                )
                setTextColor(
                    if (index == currentCategoryIndex) 0xFFFFFFFF.toInt()
                    else 0xFF000000.toInt()
                )
                setOnClickListener {
                    switchCategory(index)
                }
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

        val category = SymbolData.categories.getOrNull(currentCategoryIndex) ?: return
        val symbols = category.symbols.joinToString("")

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
                        onSymbolClick(symbols[i].toString())
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