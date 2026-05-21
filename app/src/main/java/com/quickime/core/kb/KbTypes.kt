package com.quickime.core.kb

/**
 * 知识库分类
 */
enum class KbCategory(val displayName: String) {
    General("通用"),
    Work("工作"),
    Life("生活"),
    Business("商务"),
    FaqReply("FAQ回复"),
    ComplaintHandle("投诉处理"),
    OrderInquiry("订单咨询"),
    ProductInfo("产品咨询"),
    ShippingInfo("物流信息"),
    RefundProcess("退款流程")
}

/**
 * 知识库条目
 */
data class KbEntry(
    val id: Long = 0,
    val content: String,
    val category: KbCategory = KbCategory.General,
    val source: String = "",
    val useCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 知识库检索结果
 */
data class KbResult(
    val entry: KbEntry,
    val similarity: Float
)

/**
 * 知识库统计
 */
data class KbStats(
    val category: KbCategory,
    val entryCount: Int,
    val totalSize: Long
)
