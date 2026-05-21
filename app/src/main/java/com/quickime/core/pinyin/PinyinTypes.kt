package com.quickime.core.pinyin

/**
 * 拼音输入状态
 */
enum class PinyinState {
    Idle,           // 空闲状态
    Typing,         // 正在输入拼音
    Selecting,      // 候选选择状态
}

/**
 * 拼音候选词
 */
data class PinyinCandidate(
    val text: String,           // 候选文字
    val fullPinyin: String,      // 完整拼音
    val shortPinyin: String,      // 简拼（如"wh"对应"王浩"）
    val frequency: Int = 0,     // 词频
    val isUserPhrase: Boolean = false  // 是否用户词
)

/**
 * 拼音输入会话
 */
data class PinyinSession(
    val pinyin: String = "",
    val candidates: List<PinyinCandidate> = emptyList(),
    val selectedIndex: Int = -1,
    val pageIndex: Int = 0,
    val state: PinyinState = PinyinState.Idle
) {
    val currentPageCandidates: List<PinyinCandidate>
        get() {
            val pageSize = 9
            val start = pageIndex * pageSize
            return candidates.drop(start).take(pageSize)
        }

    val hasNextPage: Boolean
        get() = (pageIndex + 1) * 9 < candidates.size

    val hasPrevPage: Boolean
        get() = pageIndex > 0

    val totalPages: Int
        get() = (candidates.size + 8) / 9
}

/**
 * 拼音工具类
 */
object PinyinUtil {

    private val initials = setOf(
        "b", "p", "m", "f", "d", "t", "n", "l",
        "g", "k", "h", "j", "q", "x",
        "zh", "ch", "sh", "r", "z", "c", "s", "y", "w"
    )

    private val finals = mapOf(
        // 单韵母
        "a" to "a", "o" to "o", "e" to "e", "i" to "i", "u" to "u", "v" to "v",
        // 复韵母
        "ai" to "ai", "ei" to "ei", "ao" to "ao", "ou" to "ou",
        "ie" to "ie", "ve" to "ve",
        // 鼻韵母
        "an" to "an", "en" to "en", "ang" to "ang", "eng" to "eng",
        // 特殊
        "er" to "er",
        // 介母 + 韵母
        "ia" to "ia", "iao" to "iao", "iu" to "iu", "ie" to "ie",
        "ua" to "ua", "uo" to "uo", "uai" to "uai", "ui" to "ui",
        "uo" to "uo", "u" to "u",
        "ve" to "ve",
        "iang" to "iang", "iong" to "iong", "iang" to "iang",
        "uang" to "uang", "ueng" to "ueng"
    )

    /**
     * 检查是否是声母
     */
    fun isInitial(s: String): Boolean = initials.contains(s)

    /**
     * 检查是否是韵母开头
     */
    fun isVowel(c: Char): Boolean = c in "aeiouü"

    /**
     * 分离声母和韵母
     */
    fun splitInitialFinal(pinyin: String): Pair<String, String> {
        if (pinyin.length < 2) return "" to pinyin

        // 检查三字母声母
        if (pinyin.length >= 3) {
            val triple = pinyin.substring(0, 3)
            if (initials.contains(triple)) {
                return triple to pinyin.substring(3)
            }
        }

        // 检查双字母声母
        if (pinyin.length >= 2) {
            val double = pinyin.substring(0, 2)
            if (initials.contains(double)) {
                return double to pinyin.substring(2)
            }
        }

        // 检查单字母声母
        if (pinyin.length >= 1) {
            val single = pinyin[0].toString()
            if (initials.contains(single)) {
                return single to pinyin.substring(1)
            }
        }

        return "" to pinyin
    }

    /**
     * 获取简拼（声母首字母）
     */
    fun getShortPinyin(fullPinyin: String): String {
        val parts = fullPinyin.split("'")
        return parts.mapNotNull { part ->
            val (initial, _) = splitInitialFinal(part)
            initial.firstOrNull()?.lowercaseChar()
        }.joinToString("")
    }
}