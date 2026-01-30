package com.pet.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * LINE Webhook 請求結構
 * https://developers.line.biz/en/reference/messaging-api/#webhook-event-objects
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LineWebhookRequest(
    val destination: String = "",
    val events: List<LineWebhookEvent> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LineWebhookEvent(
    val type: String = "",           // message, postback, follow, unfollow...
    val timestamp: Long = 0,
    val source: LineEventSource = LineEventSource(),
    val replyToken: String? = null,
    val message: LineEventMessage? = null,
    val postback: LinePostback? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LineEventSource(
    val type: String = "",           // user, group, room
    val userId: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LineEventMessage(
    val type: String = "",           // text, image, video, audio, file, location, sticker
    val id: String = "",
    val text: String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class LinePostback(
    val data: String = ""
)
