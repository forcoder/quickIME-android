package com.quickime.core.wubi

/**
 * 五笔编码结构
 * @property code 编码（1-4位）
 * @property charCount 汉字数量
 */
data class WubiCode(
    val code: String,
    val charCount: Int = code.length
)

/**
 * 候选词项
 * @property text 文本
 * @property code 编码
 * @property frequency 使用频率
 */
data class WubiCandidate(
    val text: String,
    val code: String,
    val frequency: Int = 0
)

/**
 * 输入模式
 */
enum class WubiVersion {
    Wubi86,   // 86版
    Wubi98,   // 98版
    WubiNew   // 新世纪版
}

/**
 * 五笔编码器接口
 */
interface WubiEncoder {
    /**
     * 根据编码查询候选词
     */
    fun query(code: String): List<WubiCandidate>

    /**
     * 根据文本查询编码
     */
    fun encode(text: String): List<String>

    /**
     * 根据已输入汉字联想后续词组
     */
    fun associate(prefix: String): List<WubiCandidate>

    /**
     * 获取编码提示
     */
    fun getCodeHint(text: String): String?

    /**
     * 检查编码是否有效
     */
    fun isValidCode(code: String): Boolean

    /**
     * 获取编码长度
     */
    fun getCodeLength(code: String): Int
}
