package com.quickime.android.ime.keyboard

/**
 * 键盘布局定义
 */
object KeyboardLayout {

    /**
     * QWERTY 键盘布局
     */
    val qwertyRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )

    /**
     * 五笔编码键盘布局
     */
    val wubiRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("z", "x", "c", "v", "b", "n", "m")
    )

    /**
     * 获取普通键盘行的标签
     */
    fun getRowLabel(key: String, inputMode: InputMode): String {
        return when (inputMode) {
            InputMode.Pinyin -> key.uppercase()
            InputMode.WuBi -> key.uppercase()
            InputMode.Symbol -> key
        }
    }

    /**
     * 检查是否是五笔编码键
     */
    fun isWubiKey(key: String): Boolean {
        return key.lowercase() in listOf(
            "q", "w", "e", "r", "t", "y", "u", "i", "o", "p",
            "a", "s", "d", "f", "g", "h", "j", "k", "l",
            "z", "x", "c", "v", "b", "n", "m"
        )
    }

    /**
     * 检查是否是拼音键
     */
    fun isPinyinKey(key: String): Boolean {
        return key.lowercase() in "abcdefghijklmnopqrstuvwxyz"
    }

    /**
     * 获取键盘高度
     */
    fun getKeyboardHeight(context: android.content.Context, rowCount: Int = 4): Int {
        val density = context.resources.displayMetrics.density
        val rowHeight = 48 * density
        val topBarHeight = 40 * density
        return (topBarHeight + rowHeight * rowCount + 16 * density).toInt()
    }
}