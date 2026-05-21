package com.quickime.core.pinyin

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 拼音输入法引擎
 * 支持全拼、简拼、模糊音等
 */
@Singleton
class PinyinEngine @Inject constructor() {

    private var currentSession = PinyinSession()

    // 用户词频数据
    private val userPhrases = mutableMapOf<String, MutableMap<String, Int>>()

    // 模糊音配置
    private val fuzzyMap = mapOf(
        "zh" to setOf("z"),
        "ch" to setOf("c"),
        "sh" to setOf("s"),
        "an" to setOf("ang"),
        "en" to setOf("eng"),
        "ing" to setOf("ing"),
        "iang" to setOf("iang"),
        "uang" to setOf("uang")
    )

    /**
     * 获取当前会话
     */
    fun getSession(): PinyinSession = currentSession

    /**
     * 开始输入
     */
    fun startInput() {
        currentSession = PinyinSession(state = PinyinState.Typing)
    }

    /**
     * 输入字符
     */
    fun inputChar(c: Char) {
        if (c == '\'') {
            // 分词符，直接继续
            currentSession = currentSession.copy(
                pinyin = currentSession.pinyin + c,
                state = PinyinState.Typing
            )
        } else if (c in 'a'..'z' || c in 'A'..'Z') {
            currentSession = currentSession.copy(
                pinyin = currentSession.pinyin + c.lowercaseChar(),
                state = PinyinState.Typing
            )
            updateCandidates()
        }
    }

    /**
     * 删除字符
     */
    fun deleteChar(): Boolean {
        return if (currentSession.pinyin.isNotEmpty()) {
            currentSession = currentSession.copy(
                pinyin = currentSession.pinyin.dropLast(1),
                state = if (currentSession.pinyin.isEmpty()) PinyinState.Idle else PinyinState.Typing
            )
            updateCandidates()
            true
        } else {
            false
        }
    }

    /**
     * 清空输入
     */
    fun clear() {
        currentSession = PinyinSession()
    }

    /**
     * 选择候选词
     */
    fun selectCandidate(index: Int): String? {
        val page = currentSession.currentPageCandidates
        if (index < 0 || index >= page.size) return null

        val candidate = page[index]
        currentSession = PinyinSession()

        // 更新词频
        increaseFrequency(currentSession.pinyin, candidate.text)

        return candidate.text
    }

    /**
     * 翻页
     */
    fun nextPage(): Boolean {
        if (!currentSession.hasNextPage) return false
        currentSession = currentSession.copy(pageIndex = currentSession.pageIndex + 1)
        return true
    }

    fun prevPage(): Boolean {
        if (!currentSession.hasPrevPage) return false
        currentSession = currentSession.copy(pageIndex = currentSession.pageIndex - 1)
        return true
    }

    /**
     * 更新候选词
     */
    private fun updateCandidates() {
        val pinyin = currentSession.pinyin
        if (pinyin.isEmpty()) {
            currentSession = currentSession.copy(candidates = emptyList())
            return
        }

        val candidates = mutableListOf<PinyinCandidate>()

        // 解析拼音（支持分词）
        val parts = pinyin.split("'")

        // 全拼匹配
        if (!pinyin.contains("'")) {
            // 单字全拼
            val results = PinyinTable.fuzzyMatch(pinyin)
            for ((py, chars) in results) {
                for (char in chars) {
                    candidates.add(
                        PinyinCandidate(
                            text = char,
                            fullPinyin = py,
                            shortPinyin = PinyinUtil.getShortPinyin(py),
                            frequency = userPhrases[py]?.get(char) ?: 0
                        )
                    )
                }
            }
        } else {
            // 词组全拼（如 "zhong'guo" -> "中国"）
            val words = buildWordsFromPinyin(parts)
            for (word in words) {
                candidates.add(
                    PinyinCandidate(
                        text = word,
                        fullPinyin = pinyin,
                        shortPinyin = PinyinUtil.getShortPinyin(pinyin),
                        frequency = userPhrases[pinyin]?.get(word) ?: 0,
                        isUserPhrase = true
                    )
                )
            }
        }

        // 按词频排序
        candidates.sortByDescending { it.frequency }

        currentSession = currentSession.copy(
            candidates = candidates,
            state = if (candidates.isNotEmpty()) PinyinState.Selecting else PinyinState.Typing
        )
    }

    /**
     * 从分词拼音构建词组
     */
    private fun buildWordsFromPinyin(parts: List<String>): List<String> {
        if (parts.size == 1) {
            return PinyinTable.getCandidates(parts[0])
        }

        // 简单的二元词组组合
        val results = mutableListOf<String>()
        for (i in 0 until parts.size - 1) {
            val py1 = parts[i]
            val py2 = parts[i + 1]

            val chars1 = PinyinTable.getCandidates(py1)
            val chars2 = PinyinTable.getCandidates(py2)

            for (c1 in chars1) {
                for (c2 in chars2) {
                    results.add(c1 + c2)
                }
            }
        }

        return results
    }

    /**
     * 增加词频
     */
    private fun increaseFrequency(pinyin: String, word: String) {
        userPhrases.getOrPut(pinyin) { mutableMapOf() }
            .let { it[word] = (it[word] ?: 0) + 1 }
    }

    /**
     * 启用模糊音
     */
    fun enableFuzzy(s: String, vararg alternatives: String) {
        // 可以动态添加模糊音配置
    }

    /**
     * 获取当前输入的拼音
     */
    fun getCurrentPinyin(): String = currentSession.pinyin

    /**
     * 是否为空闲状态
     */
    fun isIdle(): Boolean = currentSession.state == PinyinState.Idle

    /**
     * 是否有候选项
     */
    fun hasCandidates(): Boolean = currentSession.candidates.isNotEmpty()
}