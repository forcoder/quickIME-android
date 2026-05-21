package com.quickime.android.ime.keyboard

/**
 * 输入法模式
 */
enum class InputMode {
    WuBi,      // 五笔模式
    Pinyin,    // 拼音模式
    Symbol     // 符号模式
}

/**
 * 候选词
 */
data class Candidate(
    val text: String,
    val type: CandidateType = CandidateType.Normal,
    val source: String? = null
)

/**
 * 候选词类型
 */
enum class CandidateType {
    Normal,       // 普通候选
    Wubi,         // 五笔编码
    Pinyin,      // 拼音
    Knowledge,   // 知识库
    AI           // AI生成
}

/**
 * 键盘状态
 */
data class KeyboardState(
    val inputMode: InputMode = InputMode.WuBi,
    val currentInput: String = "",
    val candidates: List<Candidate> = emptyList(),
    val selectedIndex: Int = 0,
    val pageIndex: Int = 0,
    val showPinyin: String = "",  // 显示的拼音
    val isUpperCase: Boolean = false,
    val showCandidates: Boolean = true
) {
    val pageSize = 9

    val currentPageCandidates: List<Candidate>
        get() {
            val start = pageIndex * pageSize
            return candidates.drop(start).take(pageSize)
        }

    val hasNextPage: Boolean
        get() = (pageIndex + 1) * pageSize < candidates.size

    val hasPrevPage: Boolean
        get() = pageIndex > 0

    val totalPages: Int
        get() = (candidates.size + pageSize - 1) / pageSize
}

/**
 * 键盘按键
 */
data class Key(
    val label: String,
    val code: String,
    val width: Float = 1f,
    val isSpecial: Boolean = false,
    val action: KeyAction = KeyAction.Input
)

/**
 * 按键动作
 */
enum class KeyAction {
    Input,      // 普通输入
    Delete,     // 删除
    Space,      // 空格
    Enter,      // 回车
    ModeSwitch, // 模式切换
    PageUp,     // 上一页
    PageDown,   // 下一页
    Symbol      // 符号面板
}