package com.pet.security

import org.slf4j.LoggerFactory
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.security.Principal

/**
 * WebSocket STOMP 層 JWT 認證攔截器
 *
 * HTTP handshake（/ws）放行，認證在 STOMP CONNECT 時處理：
 * 1. 從 STOMP CONNECT header 取出 Authorization: Bearer {token}
 * 2. 用 JwtService 驗證 token
 * 3. 設定 accessor.user = Principal（userId）
 * 4. convertAndSendToUser 靠 Principal.getName() 路由到正確的使用者
 */
@Component
class WebSocketAuthInterceptor(
    private val jwtService: JwtService
) : ChannelInterceptor {

    private val log = LoggerFactory.getLogger(WebSocketAuthInterceptor::class.java)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)
            ?: return message

        if (accessor.command == StompCommand.CONNECT) {
            val authHeader = accessor.getFirstNativeHeader("Authorization")
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                val token = authHeader.substring(7)
                try {
                    if (jwtService.validateToken(token)) {
                        val userId = jwtService.extractUserId(token).toString()
                        val authorities = jwtService.extractAuthorities(token)
                        val authentication = UsernamePasswordAuthenticationToken(
                            userId, null, authorities
                        )
                        accessor.user = authentication
                        log.debug("WebSocket 使用者認證成功: {}", userId)
                    } else {
                        log.warn("WebSocket JWT 驗證失敗")
                    }
                } catch (e: Exception) {
                    log.error("WebSocket 認證錯誤: {}", e.message)
                }
            } else {
                log.warn("WebSocket CONNECT 缺少 Authorization header")
            }
        }

        return message
    }
}
