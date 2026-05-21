package com.quickime.core.wubi

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 五笔输入法引擎
 */
@Singleton
class WubiEngine @Inject constructor(
    private val codeTable: WubiCodeTable86 = WubiCodeTable86
) : WubiEncoder {

    // 候选词缓存
    private val candidateCache = mutableMapOf<String, List<WubiCandidate>>()

    // 编码历史
    private val codeHistory = mutableListOf<String>()

    // 当前编码
    private var currentCode = StringBuilder()

    override fun query(code: String): List<WubiCandidate> {
        // 检查缓存
        candidateCache[code]?.let { return it }

        // 查询编码表
        val candidates = codeTable.query(code)

        // 缓存结果
        if (candidates.isNotEmpty()) {
            candidateCache[code] = candidates
        }

        return candidates
    }

    override fun encode(text: String): List<String> {
        return codeTable.encode(text)
    }

    override fun associate(prefix: String): List<WubiCandidate> {
        // 根据已输入汉字联想词组
        val candidates = mutableListOf<WubiCandidate>()

        // 获取以prefix开头的词组
        candidates.addAll(
            WubiCodeTable86.query(prefix).filter {
                it.text.startsWith(prefix) && it.text != prefix
            }
        )

        return candidates.take(10)
    }

    override fun getCodeHint(text: String): String? {
        return codeTable.encode(text).firstOrNull()
    }

    override fun isValidCode(code: String): Boolean {
        return codeTable.isValidCode(code)
    }

    override fun getCodeLength(code: String): Int {
        return codeTable.getCodeLength(code)
    }

    /**
     * 追加编码字符
     */
    fun appendCode(char: Char): Boolean {
        if (!isValidCode(char.toString())) return false

        currentCode.append(char.uppercaseChar())

        // 限制编码长度为4
        if (currentCode.length > 4) {
            currentCode.deleteAt(0)
        }

        codeHistory.add(currentCode.toString())
        return true
    }

    /**
     * 删除最后一个编码字符
     */
    fun backspace(): String {
        if (currentCode.isNotEmpty()) {
            currentCode.deleteAt(currentCode.length - 1)
        }
        return currentCode.toString()
    }

    /**
     * 清空编码
     */
    fun clear() {
        currentCode.clear()
    }

    /**
     * 获取当前编码
     */
    fun getCurrentCode(): String = currentCode.toString()

    /**
     * 获取当前候选词列表
     */
    fun getCandidates(): List<WubiCandidate> {
        return query(currentCode.toString())
    }

    /**
     * 是否正在输入编码
     */
    fun isComposing(): Boolean = currentCode.isNotEmpty()

    /**
     * 选择候选词
     */
    fun selectCandidate(index: Int): String? {
        val candidates = getCandidates()
        if (index in candidates.indices) {
            clear()
            return candidates[index].text
        }
        return null
    }

    /**
     * 选择第一个候选词
     */
    fun selectFirst(): String? {
        return selectCandidate(0)
    }
}
