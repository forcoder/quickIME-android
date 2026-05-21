package com.quickime.core.cs

import com.quickime.core.ai.AISuggestion
import com.quickime.core.ai.AISuggestionType
import com.quickime.core.ai.InferenceEngine
import com.quickime.core.kb.KbCategory
import com.quickime.core.kb.KbResult
import com.quickime.core.kb.KnowledgeBase
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 客服建议来源
 */
enum class SuggestionSource {
    KnowledgeBase,
    AIGenerated,
    Hybrid,
    Template
}

/**
 * 客服建议
 */
data class CSSuggestion(
    val text: String,
    val source: SuggestionSource,
    val confidence: Float = 0.0f,
    val category: KbCategory = KbCategory.General,
    val aiType: AISuggestionType? = null
)

/**
 * 对话模式
 */
enum class DialogMode {
    Inquiry,      // 咨询
    Complaint,    // 投诉
    OrderQuery,   // 订单查询
    Refund,       // 退款
    General       // 一般对话
}

/**
 * 客服场景配置
 */
data class CSConfig(
    val aiEnabled: Boolean = true,
    val kbEnabled: Boolean = true,
    val aiWeight: Float = 0.5f,
    val kbWeight: Float = 0.5f,
    val maxSuggestions: Int = 5,
    val minConfidence: Float = 0.3f
)

/**
 * 客服场景管理器
 * 协调知识库检索和AI推理生成客服建议
 */
@Singleton
class CustomerServiceManager @Inject constructor(
    private val knowledgeBase: KnowledgeBase,
    private val aiEngine: InferenceEngine
) {
    private val mutex = Mutex()
    private var config = CSConfig()
    private var isReady = false

    /**
     * 初始化
     */
    suspend fun initialize() {
        mutex.withLock {
            knowledgeBase.initialize()
            aiEngine.initialize()
            isReady = true
        }
    }

    /**
     * 是否就绪
     */
    fun isReady(): Boolean = isReady

    /**
     * 更新配置
     */
    fun setConfig(newConfig: CSConfig) {
        config = newConfig
    }

    fun getConfig(): CSConfig = config

    /**
     * 生成客服建议
     */
    suspend fun generateSuggestions(
        customerMessage: String,
        maxSuggestions: Int = config.maxSuggestions
    ): List<CSSuggestion> = mutex.withLock {
        if (!isReady) return emptyList()

        // 1. 识别对话模式
        val dialogMode = detectDialogMode(customerMessage)

        // 2. 获取对应分类
        val (kbFilter, aiType) = mapModeToCategory(dialogMode)

        // 3. 并行查询知识库和AI
        val kbResults = if (config.kbEnabled) {
            knowledgeBase.search(customerMessage, maxSuggestions * 2, kbFilter)
        } else {
            emptyList()
        }

        val aiResults = if (config.aiEnabled && aiEngine.isReady()) {
            when (aiType) {
                AISuggestionType.ComplaintHandle ->
                    aiEngine.generateComplaintResponse(customerMessage, "")
                AISuggestionType.OrderInquiry ->
                    aiEngine.generateOrderInquiryReply(customerMessage)
                AISuggestionType.RefundProcess ->
                    aiEngine.generateRefundResponse(customerMessage)
                AISuggestionType.FaqReply -> {
                    val kbMatch = kbResults.firstOrNull()?.entry?.content ?: ""
                    aiEngine.generateFaqReply(customerMessage, kbMatch)
                }
                else ->
                    aiEngine.generateCustomerServiceReply(customerMessage, "")
            }
        } else {
            emptyList()
        }

        // 4. 合并结果
        mergeResults(kbResults, aiResults, maxSuggestions)
    }

    /**
     * 搜索知识库
     */
    suspend fun searchKnowledgeBase(
        query: String,
        maxResults: Int = 5,
        filter: KbCategory = KbCategory.General
    ): List<KbResult> {
        return knowledgeBase.search(query, maxResults, filter)
    }

    /**
     * 导入知识库
     */
    suspend fun importKbText(
        text: String,
        category: KbCategory
    ): Boolean {
        return knowledgeBase.importText(text, category)
    }

    /**
     * 识别对话模式
     */
    private fun detectDialogMode(message: String): DialogMode {
        val keywords = mapOf(
            DialogMode.Complaint to listOf("投诉", "不满意", "差评", "欺骗", "态度差"),
            DialogMode.OrderQuery to listOf("订单", "快递", "发货", "物流", "到哪了"),
            DialogMode.Refund to listOf("退款", "退货", "退钱", "取消订单"),
            DialogMode.Inquiry to listOf("请问", "咨询", "怎么", "多少", "价格")
        )

        for ((mode, words) in keywords) {
            if (words.any { message.contains(it) }) {
                return mode
            }
        }

        return DialogMode.General
    }

    /**
     * 映射对话模式到分类
     */
    private fun mapModeToCategory(mode: DialogMode): Pair<KbCategory, AISuggestionType> {
        return when (mode) {
            DialogMode.Complaint -> KbCategory.ComplaintHandle to AISuggestionType.ComplaintHandle
            DialogMode.OrderQuery -> KbCategory.OrderInquiry to AISuggestionType.OrderInquiry
            DialogMode.Refund -> KbCategory.RefundProcess to AISuggestionType.RefundProcess
            DialogMode.Inquiry -> KbCategory.FaqReply to AISuggestionType.FaqReply
            DialogMode.General -> KbCategory.General to AISuggestionType.CustomerService
        }
    }

    /**
     * 合并知识和AI建议
     */
    private fun mergeResults(
        kbResults: List<KbResult>,
        aiResults: List<AISuggestion>,
        maxCount: Int
    ): List<CSSuggestion> {
        val merged = mutableListOf<CSSuggestion>()

        // 添加知识库结果
        for (result in kbResults) {
            if (merged.size >= maxCount) break
            merged.add(
                CSSuggestion(
                    text = result.entry.content,
                    source = SuggestionSource.KnowledgeBase,
                    confidence = result.similarity * config.kbWeight,
                    category = result.entry.category
                )
            )
        }

        // 添加AI结果
        for (result in aiResults) {
            if (merged.size >= maxCount) break
            merged.add(
                CSSuggestion(
                    text = result.text,
                    source = SuggestionSource.AIGenerated,
                    confidence = result.confidence * config.aiWeight,
                    aiType = result.type
                )
            )
        }

        // 按置信度排序
        return merged.sortedByDescending { it.confidence }
            .filter { it.confidence >= config.minConfidence }
            .take(maxCount)
    }
}
