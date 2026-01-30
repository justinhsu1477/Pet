package com.pet.service

import com.pet.config.LineMessagingConfig
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.UUID

/**
 * LINE Content API 服務
 * - 下載圖片/影片等多媒體內容
 * - 發送回覆訊息
 */
@Service
class LineContentService(
    private val config: LineMessagingConfig
) {
    private val log = LoggerFactory.getLogger(LineContentService::class.java)
    private val restTemplate = RestTemplate()

    companion object {
        private const val CONTENT_API_URL = "https://api-data.line.me/v2/bot/message/%s/content"
        private const val REPLY_API_URL = "https://api.line.me/v2/bot/message/reply"
        private const val PUSH_API_URL = "https://api.line.me/v2/bot/message/push"
    }

    /**
     * 從 LINE Content API 下載圖片
     */
    fun downloadImage(messageId: String): ByteArray {
        val url = String.format(CONTENT_API_URL, messageId)

        val headers = HttpHeaders()
        headers.setBearerAuth(config.getChannelToken())

        val entity = HttpEntity<String>(headers)
        val response = restTemplate.exchange(url, HttpMethod.GET, entity, ByteArray::class.java)

        if (!response.statusCode.is2xxSuccessful || response.body == null) {
            throw IllegalStateException("LINE 圖片下載失敗: HTTP ${response.statusCode}")
        }

        log.info("LINE 圖片下載成功: messageId={}, size={}bytes", messageId, response.body!!.size)
        return response.body!!
    }

    /**
     * 發送回覆訊息（使用 replyToken）
     */
    fun sendReplyMessage(replyToken: String, text: String) {
        try {
            val headers = createAuthHeaders()

            val body = mapOf(
                "replyToken" to replyToken,
                "messages" to listOf(
                    mapOf("type" to "text", "text" to text)
                )
            )

            val entity = HttpEntity(body, headers)
            restTemplate.postForEntity(REPLY_API_URL, entity, String::class.java)
            log.info("LINE 回覆訊息發送成功")
        } catch (e: Exception) {
            log.error("LINE 回覆訊息發送失敗: {}", e.message)
        }
    }

    /**
     * 發送帶 Quick Reply 按鈕的回覆訊息
     * 用於寵物選擇流程：列出保母的寵物讓其點選
     *
     * @param replyToken LINE 回覆 token
     * @param text 訊息文字
     * @param pets 寵物清單 (petId, petName)
     */
    fun sendPetSelectionReply(replyToken: String, text: String, pets: List<Pair<UUID, String>>) {
        try {
            val headers = createAuthHeaders()

            val quickReplyItems = pets.map { (petId, petName) ->
                mapOf(
                    "type" to "action",
                    "action" to mapOf(
                        "type" to "postback",
                        "label" to petName.take(20), // LINE label 最多 20 字
                        "data" to "action=select_pet&petId=$petId",
                        "displayText" to "🐾 $petName"
                    )
                )
            }

            val body = mapOf(
                "replyToken" to replyToken,
                "messages" to listOf(
                    mapOf(
                        "type" to "text",
                        "text" to text,
                        "quickReply" to mapOf("items" to quickReplyItems)
                    )
                )
            )

            val entity = HttpEntity(body, headers)
            restTemplate.postForEntity(REPLY_API_URL, entity, String::class.java)
            log.info("LINE Quick Reply 寵物選擇發送成功: {} 隻寵物", pets.size)
        } catch (e: Exception) {
            log.error("LINE Quick Reply 發送失敗: {}", e.message)
        }
    }

    /**
     * 發送 Push 訊息（不需 replyToken，主動推送）
     */
    fun sendPushMessage(lineUserId: String, text: String) {
        try {
            val headers = createAuthHeaders()

            val body = mapOf(
                "to" to lineUserId,
                "messages" to listOf(
                    mapOf("type" to "text", "text" to text)
                )
            )

            val entity = HttpEntity(body, headers)
            restTemplate.postForEntity(PUSH_API_URL, entity, String::class.java)
            log.info("LINE Push 訊息發送成功: userId={}", lineUserId)
        } catch (e: Exception) {
            log.error("LINE Push 訊息發送失敗: {}", e.message)
        }
    }

    private fun createAuthHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(config.getChannelToken())
        return headers
    }
}
