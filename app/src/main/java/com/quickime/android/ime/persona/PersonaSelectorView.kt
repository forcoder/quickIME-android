package com.quickime.android.ime.persona

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView

class PersonaSelectorView(
    private val ctx: Context,
    private val personas: List<Persona>,
    private val currentPersona: Persona,
    private val onPersonaSelected: (Persona) -> Unit
) {

    fun showAsPopup(anchor: View) {
        val popupView = createPopupView()

        val popup = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 8f
            isFocusable = true
        }

        popup.showAtLocation(anchor, Gravity.BOTTOM, 0, anchor.height + 16)
    }

    private fun createPopupView(): View {
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        // 标题
        val title = TextView(ctx).apply {
            text = "选择人设"
            textSize = 16f
            setTextColor(0xFF333333.toInt())
            setPadding(0, 0, 0, dp(8))
        }
        container.addView(title)

        // 人设网格
        val gridLayout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
        }

        val cols = 2
        var rowLayout: LinearLayout? = null

        personas.forEachIndexed { index, persona ->
            if (index % cols == 0) {
                rowLayout = LinearLayout(ctx).apply {
                    orientation = LinearLayout.HORIZONTAL
                }
                gridLayout.addView(rowLayout)
            }

            val btn = createPersonaButton(persona)
            rowLayout?.addView(btn)
        }

        // 填充最后一行空白
        if (personas.size % cols != 0) {
            val remaining = cols - (personas.size % cols)
            rowLayout?.let {
                repeat(remaining) {
                    val spacer = View(ctx).apply {
                        layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f)
                    }
                    it.addView(spacer)
                }
            }
        }

        container.addView(gridLayout)

        return container
    }

    private fun createPersonaButton(persona: Persona): View {
        return Button(ctx).apply {
            text = "${persona.icon} ${persona.name}"
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f).apply {
                marginEnd = dp(8)
                bottomMargin = dp(8)
            }
            textSize = 14f
            gravity = Gravity.CENTER

            // 当前选中的人设高亮
            if (persona.id == currentPersona.id) {
                setBackgroundColor(0xFF2196F3.toInt())
                setTextColor(0xFFFFFFFF.toInt())
            } else {
                setBackgroundColor(0xFFF5F5F5.toInt())
                setTextColor(0xFF333333.toInt())
            }

            setOnClickListener {
                onPersonaSelected(persona)
            }
        }
    }

    private fun dp(value: Int): Int {
        return (value * ctx.resources.displayMetrics.density).toInt()
    }
}