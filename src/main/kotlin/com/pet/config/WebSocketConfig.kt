package com.pet.config

import com.pet.security.WebSocketAuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val webSocketAuthInterceptor: WebSocketAuthInterceptor
) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 啟用 in-memory broker，處理 /topic（廣播）和 /queue（點對點）
        registry.enableSimpleBroker("/topic", "/queue")
        // 前端發送訊息的前綴（此專案主要是後端推送，較少用到）
        registry.setApplicationDestinationPrefixes("/app")
        // convertAndSendToUser 會自動加上此前綴
        registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // WebSocket 連線端點，支援 SockJS fallback
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        // 在 STOMP CONNECT 時驗證 JWT
        registration.interceptors(webSocketAuthInterceptor)
    }
}
