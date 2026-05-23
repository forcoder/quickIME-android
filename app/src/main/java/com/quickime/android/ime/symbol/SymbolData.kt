package com.quickime.android.ime.symbol

object SymbolData {

    data class SymbolCategory(
        val name: String,
        val symbols: List<String>
    )

    // 近期使用符号（最近 20 个）
    private val _recentSymbols = mutableListOf<String>()
    val recentSymbols: List<String> get() = _recentSymbols.toList()

    val categories: List<SymbolCategory> = listOf(
        // 中文标点
        SymbolCategory("中文标点", listOf("，。！？；：""''（）【】……——～")),
        // 英文标点
        SymbolCategory("英文标点", listOf(",.!?;:\"'()[]{}<>-_+=@#\$%^&*|\\\\/")),
        // 常用符号
        SymbolCategory("常用", listOf("√×±°℃℉‰§№☆★○●◇◆□■△▽▼◁◀▷▶")),
        // 颜文字
        SymbolCategory("颜文字", listOf(
            "(◕‿◕)", "(｡◕‿◕｡)", "╯︿╰", "(T_T)", "(?_?)", "Orz", "orz",
            "(>_<)", "(=_=)", "(o_o)", "(*_*)", "(^_^)", "(*^-^*)", "(≧▽≦)", "/(ㄒoㄒ)/~~",
            "(^.^)", "(°o°)", "(?Д?)", "(¬_¬)"
        )),
        // 数学符号
        SymbolCategory("数学", listOf("+-×÷=≠<>≤≥≈∞∑∏√∫∂∆∇∈∉⊂⊃∪∩∝")),
        // 箭头
        SymbolCategory("箭头", listOf("←→↑↓↔↕↖↗↘↙⇐⇒⇑⇓⇔⇕➔➜➝➞➟➠")),
        // 罗马数字
        SymbolCategory("罗马", listOf("ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫⅬⅭⅮⅯⅰⅱⅲⅲⅲⅳⅴⅵⅵⅶⅷⅸⅹ")),
        // 特殊符号
        SymbolCategory("特殊", listOf("☀☁☂☃♪♫♬★☆●○◆◇■□▲△▼▽☎☏✉✁✂✔✘☒✓✗™©®™℠℗℡℻℀℁ℂℓ℅℆ℇ")),
        // 制表符
        SymbolCategory("制表", listOf("─┌┬┐├┼┤└┴┘│═╔╦╗╠╬╣╚╩╝"))
    )

    val allSymbols: List<String> = categories.flatMap { it.symbols }

    /**
     * 获取分类（包含近期符号）
     */
    fun getByIndex(index: Int): SymbolCategory? {
        if (index == 0 && recentSymbols.isNotEmpty()) {
            return SymbolCategory("近期", recentSymbols)
        }
        // 索引0是近期，所以实际分类索引需要减1
        val actualIndex = if (index == 0) 0 else index - 1
        return categories.getOrNull(actualIndex)?.let { cat ->
            if (index == 0 && recentSymbols.isNotEmpty()) {
                SymbolCategory("近期", recentSymbols)
            } else {
                cat
            }
        }
    }

    /**
     * 获取分类数量（包括近期分类）
     */
    val categoryCount: Int get() = categories.size + 1 // +1 for "近期"

    /**
     * 记录符号使用
     */
    fun onSymbolUsed(symbol: String) {
        _recentSymbols.remove(symbol)
        _recentSymbols.add(0, symbol)
        if (_recentSymbols.size > 20) {
            _recentSymbols.removeLast()
        }
    }

    /**
     * 清空近期符号
     */
    fun clearRecentSymbols() {
        _recentSymbols.clear()
    }
}