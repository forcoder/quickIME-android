package com.quickime.core.ai

import com.quickime.core.kb.KbCategory

/**
 * AI 建议类型
 */
enum class AISuggestionType(val displayName: String) {
    ShortReply("短句回复"),
    WorkPhrase("工作话术"),
    CustomerService("客服回复"),
    FaqReply("FAQ回复"),
    ComplaintHandle("投诉处理"),
    OrderInquiry("订单咨询"),
    RefundProcess("退款流程")
}

/**
 * AI 建议条目
 */
data class AISuggestion(
    val text: String,
    val type: AISuggestionType,
    val confidence: Float = 0.0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Prompt 模板
 */
data class PromptTemplate(
    val systemPrompt: String,
    val inputPrefix: String,
    val maxTokens: Int = 50,
    val temperature: Float = 0.7f
)
