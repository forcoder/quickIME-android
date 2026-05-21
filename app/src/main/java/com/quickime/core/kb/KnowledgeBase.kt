package com.quickime.core.kb

import android.content.Context
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 知识库管理器
 */
@Singleton
class KnowledgeBase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val db: KbDatabase by lazy {
        Room.databaseBuilder(
            context,
            KbDatabase::class.java,
            KbDatabase.DATABASE_NAME
        ).build()
    }

    private val dao: KbDao by lazy { db.kbDao() }

    /**
     * 初始化知识库
     */
    suspend fun initialize() {
        // 可以在这里预加载默认知识库
        insertDefaultEntries()
    }

    /**
     * 搜索知识库
     */
    suspend fun search(query: String, maxResults: Int = 10, filter: KbCategory? = null): List<KbResult> {
        // 关键词搜索
        val keywordResults = dao.searchByKeyword(query, maxResults)
            .first()
            .map { KbResult(it.toEntry(), 1.0f) }

        // 应用分类过滤
        val filtered = if (filter != null) {
            keywordResults.filter { it.entry.category == filter }
        } else {
            keywordResults
        }

        // 增加使用计数
        filtered.forEach { dao.incrementUseCount(it.entry.id) }

        return filtered
    }

    /**
     * 混合搜索（语义+关键词）
     */
    suspend fun hybridSearch(
        query: String,
        maxResults: Int = 10,
        filter: KbCategory = KbCategory.General,
        semanticWeight: Float = 0.7f,
        keywordWeight: Float = 0.3f
    ): List<KbResult> {
        return search(query, maxResults, filter)
    }

    /**
     * 获取分类统计
     */
    suspend fun getStats(): List<KbStats> {
        return dao.getStats().map {
            KbStats(it.category, it.count, it.size)
        }
    }

    /**
     * 获取所有条目
     */
    fun getEntries(offset: Int = 0, limit: Int = 100): Flow<List<KbEntry>> {
        return dao.getEntries(offset, limit).map { list ->
            list.map { it.toEntry() }
        }
    }

    /**
     * 导入文本
     */
    suspend fun importText(text: String, category: KbCategory, source: String = ""): Boolean {
        val chunks = chunkText(text)
        val entities = chunks
            .filter { it.length >= 5 }
            .map { content ->
                KbEntryEntity(
                    content = content,
                    category = category,
                    source = source,
                    useCount = 0,
                    createdAt = System.currentTimeMillis()
                )
            }

        if (entities.isNotEmpty()) {
            dao.insertAll(entities)
        }
        return true
    }

    /**
     * 删除条目
     */
    suspend fun delete(id: Long): Boolean {
        dao.getById(id)?.let {
            dao.delete(it)
            return true
        }
        return false
    }

    /**
     * 更新分类
     */
    suspend fun updateCategory(id: Long, category: KbCategory) {
        dao.updateCategory(id, category)
    }

    /**
     * 文本分块
     */
    private fun chunkText(text: String): List<String> {
        // 按段落分块
        val chunks = mutableListOf<String>()
        val paragraphs = text.split(Regex("[\n\r]+"))

        val currentChunk = StringBuilder()
        for (paragraph in paragraphs) {
            if (paragraph.length < 10) {
                if (currentChunk.isNotEmpty()) {
                    currentChunk.append(" ")
                    currentChunk.append(paragraph)
                }
            } else {
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString().trim())
                    currentChunk.clear()
                }
                // 如果段落过长，进一步分句
                val sentences = paragraph.split(Regex("[。！？；]"))
                for (sentence in sentences) {
                    if (sentence.length >= 10) {
                        chunks.add(sentence.trim())
                    } else if (currentChunk.isNotEmpty()) {
                        currentChunk.append(sentence)
                    }
                }
            }
        }

        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString().trim())
        }

        return chunks
    }

    /**
     * 插入默认知识库条目
     */
    private suspend fun insertDefaultEntries() {
        if (dao.countAll() > 0) return

        val defaultEntries = listOf(
            // FAQ 回复
            KbEntry(content = "您好，感谢您的咨询。请问有什么可以帮助您的？", category = KbCategory.FaqReply),
            KbEntry(content = "关于您的问题，我建议您查看我们的帮助中心获取更多信息。", category = KbCategory.FaqReply),
            KbEntry(content = "如需进一步帮助，请拨打客服热线：400-xxx-xxxx", category = KbCategory.FaqReply),

            // 投诉处理
            KbEntry(content = "非常抱歉给您带来不便，我们会认真对待您的反馈。", category = KbCategory.ComplaintHandle),
            KbEntry(content = "感谢您的耐心，我们会立即核实情况并处理。", category = KbCategory.ComplaintHandle),
            KbEntry(content = "我们承诺会在24小时内给您满意的答复。", category = KbCategory.ComplaintHandle),

            // 订单咨询
            KbEntry(content = "您好，请问您的订单号是多少？我来帮您查询。", category = KbCategory.OrderInquiry),
            KbEntry(content = "您的订单正在处理中，预计3-5个工作日内发货。", category = KbCategory.OrderInquiry),
            KbEntry(content = "您可以在'我的订单'中查看最新的物流进度。", category = KbCategory.OrderInquiry),

            // 退款流程
            KbEntry(content = "您好，退款申请已受理，预计3-7个工作日原路返回。", category = KbCategory.RefundProcess),
            KbEntry(content = "退款审核通过后会在1-3个工作日到账，请耐心等待。", category = KbCategory.RefundProcess),
            KbEntry(content = "您可以在订单详情中查看退款进度。", category = KbCategory.RefundProcess)
        )

        defaultEntries.forEach {
            dao.insert(KbEntryEntity.fromEntry(it))
        }
    }
}
