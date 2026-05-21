package com.quickime.core.wubi

/**
 * 86版五笔码表（部分示例）
 * 实际应用中应从外部文件加载完整码表
 */
object WubiCodeTable86 {

    // 单笔画键：横1竖2撇3捺4折5
    // 键名字根表（简化版）
    private val keyMap = mapOf(
        // 横区 (G=11, F=12, D=13, S=14, A=15)
        'G' to "一", 'F' to "二", 'D' to "三",
        'S' to "木", 'A' to "工",
        // 竖区 (H=21, J=22, K=23, L=24, M=25)
        'H' to "目", 'J' to "日", 'K' to "口", 'L' to "田", 'M' to "山",
        // 撇区 (T=31, R=32, E=33, W=34, Q=35)
        'T' to "禾", 'R' to "白", 'E' to "月", 'W' to "人", 'Q' to "金",
        // 捺区 (Y=41, U=42, I=43, O=44, P=45)
        'Y' to "言", 'U' to "立", 'I' to "水", 'O' to "火", 'P' to "之",
        // 折区 (N=51, B=52, V=53, C=54, X=55)
        'N' to "已", 'B' to "子", 'V' to "女", 'C' to "又", 'X' to "纟"
    )

    // 常用汉字编码表（简化版，实际应加载完整表）
    private val codeMap = mapOf(
        "的" to "R", "我" to "Q", "了" to "B", "人" to "W",
        "在" to "S", "有" to "E", "和" to "T", "就" to "B",
        "不" to "I", "他" to "W", "这" to "P", "中" to "K",
        "大" to "D", "为" to "O", "上" to "U", "个" to "Q",
        "来" to "U", "到" to "GC", "时" to "J", "出" to "BM",
        "你" to "W", "会" to "WF", "子" to "B", "那" to "VFB",
        "得" to "TJ", "说" to "YUT", "们" to "WU", "地" to "F",
        "也" to "BN", "子" to "B", "学" to "IPBF", "如" to "VK",
        "要" to "SV", "就" to "YB", "去" to "FCU", "年" to "RH",
        "能" to "CE", "进" to "CA", "对" to "CFY", "起" to "FH",
        "过" to "TP", "业" to "OJG", "开" to "GK", "道" to "UHP",
        "里" to "JFD", "后" to "RG", "自" to "J", "于" to "F",
        "下" to "GH", "本" to "SG", "等" to "TFFU", "还" to "GIPI",
        "发" to "V", "在" to "S", "场" to "NMR", "从" to "W",
        "当" to "IV", "度" to "YACI", "家" to "PE", "方" to "YY",
        "关" to "UD", "点" to "HK", "成" to "DNT", "行" to "TF",
        "知" to "TD", "以" to "C", "长" to "TA", "见" to "UQ",
        "无" to "FQ", "形" to "GA", "间" to "UJ", "新" to "UJR",
        "去" to "FC", "动" to "FC", "身" to "TM", "电" to "GB",
        "感" to "DGKN", "想" to "SHNU", "再" to "SM", "查" to "SJR",
        "问" to "UK", "却" to "FCB", "表" to "GE", "示" to "FI",
        "老" to "FT", "由" to "MH", "或" to "AK", "页" to "DMU",
        "数" to "OV", "意" to "YNCU", "只" to "KW", "主" to "Y",
        "义" to "YQ", "各" to "TK", "员" to "WJ", "立" to "U",
        "提" to "RJ", "解" to "QE", "决" to "UN", "题" to "JQR",
        "意" to "YNCU", "见" to "UQ", "程" to "TKGG", "规" to "FWC",
        "保" to "WK", "至" to "GCF", "作" to "WT", "业" to "OJG",
        "况" to "UKQ", "真" to "FHNU", "品" to "KKGF", "总" to "UKE",
        "管" to "TPN", "系" to "TX", "权" to "SC", "量" to "TJG",
        "务" to "TG", "且" to "EG", "记" to "YN", "需" to "DEM",
        "调" to "YMQ", "七" to "AG", "研" to "DGA", "求" to "FCI",
        "根" to "SVEY", "据" to "RND", "导" to "FKU", "器" to "KKKF",
        "百" to "DJ", "更" to "GJQ", "感" to "DGKN", "接" to "RVP",
        "提" to "RJ", "战" to "HK", "圆" to "LKMD", "打" to "RAG"
    )

    // 词组编码表（简化版）
    private val phraseMap = mapOf(
        "我们" to "TQWR", "中国" to "KHLG", "人民" to "WNBW",
        "工作" to "WAA", "问题" to "THNU", "他们" to "WXBW",
        "自己" to "TJRN", "没有" to "YXFP", "因为" to "YLKG",
        "如果" to "VKJS", "可以" to "SKNG", "就是" to "YBBG",
        "不是" to "YTDJ", "但是" to "KDJH", "以为" to "YCET",
        "因此" to "YXDH", "所以" to "YNR", "对于" to "CF",
        "就是" to "YBBG", "成为" to "DLBB", "进行" to "TCJP",
        "可能" to "CEPF", "一个" to "WXG", "大家" to "DPPE",
        "什么" to "WTCU", "怎么" to "WTTD", "这样" to "YPSU",
        "已经" to "YNEG", "不要" to "YNSV", "这里" to "YJPT",
        "还是" to "CIPU", "而且" to "DMJG", "或者" to "AAKG",
        "如何" to "VXSY", "东西" to "SJTG", "时间" to "JFYJ",
        "地方" to "FFYN", "公司" to "WNGN", "应该" to "YCET",
        "已经" to "YNEG", "问题" to "THNU", "开始" to "GAI",
        "以后" to "RGYY", "不会" to "WFCE", "这是" to "YPG",
        "那个" to "RBBP", "不能" to "CEXS", "只是" to "KNWY",
        "不能" to "CEXS", "如果" to "VKJS", "就是" to "YBBG"
    )

    /**
     * 查询编码对应的候选词
     */
    fun query(code: String): List<WubiCandidate> {
        val candidates = mutableListOf<WubiCandidate>()

        // 从单字表查询
        codeMap.filter { it.value == code }.forEach {
            candidates.add(WubiCandidate(it.key, code))
        }

        // 从词组表查询
        phraseMap.filter { it.value.startsWith(code) }.forEach {
            candidates.add(WubiCandidate(it.key, it.value))
        }

        return candidates.sortedByDescending { it.frequency }
    }

    /**
     * 根据文本获取编码
     */
    fun encode(text: String): List<String> {
        // 优先查词组
        phraseMap[text]?.let { return listOf(it) }

        // 查单字
        codeMap[text]?.let { return listOf(it) }

        return emptyList()
    }

    /**
     * 检查编码是否有效
     */
    fun isValidCode(code: String): Boolean {
        if (code.isEmpty() || code.length > 4) return false
        return code.all { it in "ABCDEFGHIJKLMNOPQRSTUVWXYZ" }
    }

    /**
     * 获取编码长度
     */
    fun getCodeLength(code: String): Int {
        return when {
            code.isEmpty() -> 0
            // 判断是否需要加空格（4码取1、2码取2、不足4码取3）
            else -> minOf(4, code.length)
        }
    }
}
