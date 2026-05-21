package com.quickime.core.ai

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AI 推理引擎实现
 * 支持本地模型和 API 调用
 */
@Singleton
class AiInferenceEngine @Inject constructor(
    @ApplicationContext private val context: Context
) : InferenceEngine {

    private val config = AiConfig()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private var isInitialized = false

    override suspend fun initialize() {
        withContext(Dispatchers.IO) {
            // 可以在这里初始化本地模型
            isInitialized = true
        }
    }

    override fun isReady(): Boolean = isInitialized && config.enabled

    override fun shutdown() {
        isInitialized = false
    }

    override suspend fun generateSuggestions(
        context: String,
        userInput: String,
        maxSuggestions: Int
    ): List<AISuggestion> {
        return generateCustomerServiceReply(userInput, context)
    }

    override suspend fun generateCustomerServiceReply(
        customerMessage: String,
        context: String
    ): List<AISuggestion> {
        // 模拟生成客服回复
        return listOf(
            AISuggestion("您好，感谢您的咨询，我来为您解答。", AISuggestionType.CustomerService, 0.85f),
            AISuggestion("非常理解您的情况，我们会尽快处理。", AISuggestionType.CustomerService, 0.82f),
            AISuggestion("根据您描述的情况，建议您：", AISuggestionType.CustomerService, 0.78f)
        )
    }

    override suspend fun generateFaqReply(
        question: String,
        kbMatch: String
    ): List<AISuggestion> {
        val results = mutableListOf<AISuggestion>()

        if (kbMatch.isNotEmpty()) {
            results.add(AISuggestion(kbMatch, AISuggestionType.FaqReply, 0.95f))
        }

        results.add(AISuggestion("如需更详细的帮助，请联系人工客服。", AISuggestionType.FaqReply, 0.75f))
        results.add(AISuggestion("您可以查看常见问题解答获取更多信息。", AISuggestionType.FaqReply, 0.70f))

        return results
    }

    override suspend fun generateComplaintResponse(
        complaint: String,
        context: String
    ): List<AISuggestion> {
        return listOf(
            AISuggestion("非常抱歉给您带来不好的体验，我们会认真对待。", AISuggestionType.ComplaintHandle, 0.88f),
            AISuggestion("感谢您的反馈，我们对此深表歉意。", AISuggestionType.ComplaintHandle, 0.85f),
            AISuggestion("我们承诺24小时内给您满意答复。", AISuggestionType.ComplaintHandle, 0.82f)
        )
    }

    override suspend fun generateOrderInquiryReply(
        inquiry: String
    ): List<AISuggestion> {
        return listOf(
            AISuggestion("您好，请提供订单号，我来帮您查询。", AISuggestionType.OrderInquiry, 0.82f),
            AISuggestion("您可以在'我的订单'中查看物流信息。", AISuggestionType.OrderInquiry, 0.80f),
            AISuggestion("您的订单正在处理中，预计3-5日发货。", AISuggestionType.OrderInquiry, 0.78f)
        )
    }

    override suspend fun generateRefundResponse(
        refundRequest: String
    ): List<AISuggestion> {
        return listOf(
            AISuggestion("您好，退款申请已受理，3-7个工作日到账。", AISuggestionType.RefundProcess, 0.86f),
            AISuggestion("退款流程已启动，可查看订单详情。", AISuggestionType.RefundProcess, 0.84f),
            AISuggestion("审核通过后原路返回，请耐心等待。", AISuggestionType.RefundProcess, 0.82f)
        )
    }

    /**
     * 调用 API 生成回复
     */
    private suspend fun callApi(prompt: String): String = withContext(Dispatchers.IO) {
        if (config.apiEndpoint.isEmpty()) {
            return@withContext ""
        }

        try {
            val jsonBody = JSONObject().apply {
                put("model", config.modelName)
                put("prompt", prompt)
                put("max_tokens", config.maxTokens)
                put("temperature", config.temperature)
            }

            val request = Request.Builder()
                .url(config.apiEndpoint)
                .addHeader("Authorization", "Bearer ${config.apiKey}")
                .addHeader("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string() ?: ""
                } else {
                    ""
                }
            }
        } catch (e: Exception) {
            ""
        }
    }
}
