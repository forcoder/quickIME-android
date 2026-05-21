package com.quickime.core.pinyin

/**
 * 智能联想引擎
 * 实现模糊音、智能纠错、联想词等功能
 */
class SmartInputEngine(
    private val pinyinEngine: PinyinEngine
) {
    // 用户词频历史
    private val wordFrequency = mutableMapOf<String, MutableMap<String, Int>>()
    private val recentWords = mutableListOf<String>()

    // 模糊音映射
    private val fuzzyMap = mapOf(
        // 卷舌音模糊
        "zh" to setOf("z"),
        "ch" to setOf("c"),
        "sh" to setOf("s"),
        // 前后鼻音模糊
        "an" to setOf("ang"),
        "en" to setOf("eng"),
        "in" to setOf("ing"),
        // 其他常见模糊
        "iang" to setOf("iang"),
        "uang" to setOf("uang"),
        "iong" to setOf("iong"),
        "iang" to setOf("iang")
    )

    // 智能纠错词库
    private val autoCorrectMap = mapOf(
        "n" to "ni", "l" to "li", "r" to "ri",
        "h" to "he", "e" to "e", "w" to "wo",
        "b" to "bu", "p" to "pa", "m" to "me"
    )

    /**
     * 启用模糊音
     */
    fun enableFuzzyMode(enabled: Boolean) {
        // 可以动态控制模糊音功能
    }

    /**
     * 获取联想词
     */
    fun getSuggestions(currentText: String): List<String> {
        if (currentText.isEmpty()) return emptyList()

        val suggestions = mutableListOf<String>()

        // 1. 基于用户历史词频
        suggestions.addAll(getHistorySuggestions(currentText))

        // 2. 基于常用搭配
        suggestions.addAll(getCommonPairSuggestions(currentText))

        // 3. 去重并返回前5个
        return suggestions.distinct().take(5)
    }

    /**
     * 基于历史词频的联想
     */
    private fun getHistorySuggestions(text: String): List<String> {
        return wordFrequency.entries
            .filter { (_, words) -> words.any { it.key.startsWith(text) } }
            .flatMap { (_, words) -> words.keys }
            .filter { it.startsWith(text) }
            .sortedByDescending { getWordScore(it) }
            .take(3)
    }

    /**
     * 基于常用搭配的联想
     */
    private fun getCommonPairSuggestions(text: String): List<String> {
        val commonPairs = mapOf(
            "你好" to listOf("啊", "吗", "呀", "好"),
            "谢谢" to listOf("你", "了", "啊", ""),
            "是的" to listOf("啊", "吧", ""),
            "好的" to listOf("，", "吗", "呢", ""),
            "我" to listOf("的", "是", "在", "有", "不"),
            "你" to listOf("的", "是", "好", "在", "吗"),
            "在" to listOf("哪", "吗", "这", "做", "说"),
            "不" to listOf("好", "是", "用", "用", "知道"),
            "是" to listOf("的", "不", "啊", "吗", "我")
        )

        val lastWord = text.takeLast(2)
        return commonPairs[lastWord] ?: emptyList()
    }

    /**
     * 记录用户输入
     */
    fun recordInput(word: String) {
        if (word.isEmpty()) return

        // 更新历史
        recentWords.add(word)
        if (recentWords.size > 100) {
            recentWords.removeAt(0)
        }

        // 更新词频
        val pinyin = PinyinUtil.getShortPinyin(
            word.take(4).map { getCharPinyin(it) }.joinToString("")
        )
        wordFrequency.getOrPut(pinyin) { mutableMapOf() }
            .let { it[word] = (it[word] ?: 0) + 1 }
    }

    /**
     * 智能纠错
     */
    fun autoCorrect(input: String): String? {
        // 检查是否是单字母输入
        if (input.length == 1 && input in autoCorrectMap) {
            // 提示纠错或自动修正
            return autoCorrectMap[input]
        }

        // 检查模糊音替换
        return fuzzyMatchWithCorrection(input)
    }

    /**
     * 模糊音匹配纠错
     */
    private fun fuzzyMatchWithCorrection(input: String): String? {
        for ((standard, alternatives) in fuzzyMap) {
            if (input.startsWith(standard)) {
                // 尝试用模糊音匹配
                for (alt in alternatives) {
                    val corrected = alt + input.substring(standard.length)
                    if (hasWordStartingWith(corrected)) {
                        return corrected
                    }
                }
            }
        }
        return null
    }

    /**
     * 检查是否有以某拼音开头的词
     */
    private fun hasWordStartingWith(pinyin: String): Boolean {
        return PinyinTable.fuzzyMatch(pinyin).isNotEmpty()
    }

    /**
     * 获取词语评分
     */
    private fun getWordScore(word: String): Int {
        val historyCount = wordFrequency.values.sumOf { it[word] ?: 0 }
        val recentBonus = if (recentWords.contains(word)) 10 else 0
        val lengthBonus = word.length * 2

        return historyCount * 10 + recentBonus + lengthBonus
    }

    /**
     * 获取汉字的拼音（简化实现）
     */
    private fun getCharPinyin(char: Char): String {
        // 这里简化处理，实际应该查表
        return char.toString()
    }

    /**
     * 获取高频词
     */
    fun getTopWords(count: Int = 10): List<String> {
        return wordFrequency.values
            .flatMap { it.entries }
            .sortedByDescending { it.value }
            .map { it.key }
            .take(count)
    }

    /**
     * 清理历史
     */
    fun clearHistory() {
        wordFrequency.clear()
        recentWords.clear()
    }
}