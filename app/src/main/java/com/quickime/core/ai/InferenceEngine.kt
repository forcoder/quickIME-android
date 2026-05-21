package com.quickime.core.ai

/**
 * AI 推理引擎接口
 */
interface InferenceEngine {
    suspend fun initialize()
    fun isReady(): Boolean
    fun shutdown()

    suspend fun generateSuggestions(
        context: String,
        userInput: String,
        maxSuggestions: Int = 3
    ): List<AISuggestion>

    suspend fun generateCustomerServiceReply(
        customerMessage: String,
        context: String
    ): List<AISuggestion>

    suspend fun generateFaqReply(
        question: String,
        kbMatch: String
    ): List<AISuggestion>

    suspend fun generateComplaintResponse(
        complaint: String,
        context: String
    ): List<AISuggestion>

    suspend fun generateOrderInquiryReply(
        inquiry: String
    ): List<AISuggestion>

    suspend fun generateRefundResponse(
        refundRequest: String
    ): List<AISuggestion>
}

/**
 * AI 引擎配置
 */
data class AiConfig(
    val enabled: Boolean = true,
    val apiEndpoint: String = "",
    val apiKey: String = "",
    val modelName: String = "gpt-3.5-turbo",
    val maxTokens: Int = 100,
    val temperature: Float = 0.7f
)
