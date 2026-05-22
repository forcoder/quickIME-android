package com.quickime.android.ime.symbol

object SymbolData {

    data class SymbolCategory(
        val name: String,
        val symbols: List<String>
    )

    val categories: List<SymbolCategory> = listOf(
        // 中文标点
        SymbolCategory("中文标点", listOf(
            "，。！？；：""''（）【】……——～"
        )),
        // 英文标点
        SymbolCategory("英文标点", listOf(
            ",.!?;:\"'()[]{}<>-_+=@#\$%^&*|\\/"
        )),
        // 常用符号
        SymbolCategory("常用", listOf(
            "√×±°℃℉‰§№☆★○●◇◆□■△▽▼◁◀▷▶"
        )),
        // 颜文字
        SymbolCategory("颜文字", listOf(
            "(◕‿◕)", "(｡◕‿◕｡)", "╯︿╰", "(T_T)", "(?_?)", "Orz", "orz",
            "(>_<)", "(=_=)", "(o_o)", "(=_=)", "(*_*)", "(=_=)",
            "(^_^)", "(*^-^*)", "(≧▽≦)", "/(ㄒoㄒ)/~~", "(′д` )",
            "(^.^)", "(=_=)", "(°o°)", "(?Д?)", "(¬_¬)"
        )),
        // 数学符号
        SymbolCategory("数学", listOf(
            "+-×÷=≠<>≤≥≈∞∑∏√∫∂∆∇∈∉⊂⊃∪∩∝"
        )),
        // 箭头
        SymbolCategory("箭头", listOf(
            "←→↑↓↔↕↖↗↘↙⇐⇒⇑⇓⇔⇕➔➜➝➞➟➠"
        )),
        // 罗马数字
        SymbolCategory("罗马", listOf(
            "ⅠⅡⅢⅣⅤⅥⅦⅧⅨⅩⅪⅫⅬⅭⅮⅯⅰⅱⅱⅲⅳⅴⅵⅵⅶⅷⅸⅹ"
        )),
        // 特殊符号
        SymbolCategory("特殊", listOf(
            "☀☁☂☃♪♫♬★☆●○◆◇■□▲△▼▽☎☏✉✁✂✔✘☒✓✗™©®™℠℗℡℻℀℁ℂℓ℅℆ℇ"
        )),
        // 制表符
        SymbolCategory("制表", listOf(
            "─┌┬┐├┼┤└┴┘│═╔╦╗╠╬╣╚╩╝"
        ))
    )

    val allSymbols: List<String> = categories.flatMap { it.symbols }

    fun getByIndex(index: Int): SymbolCategory? {
        return categories.getOrNull(index)
    }
}
