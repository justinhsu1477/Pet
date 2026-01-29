package com.pet.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * API 速率限制過濾器
 * - 已認證用戶：以 userId 為 key
 * - 未認證用戶：以 IP 為 key
 * - POST /api/bookings：10 次/分鐘（防搶約）
 * - 其他 API：60 次/分鐘
 */
@Component
class RateLimitFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(RateLimitFilter::class.java)

    private val generalLimiter = RateLimiter(maxTokens = 60, refillRate = 1.0)
    private val bookingLimiter = RateLimiter(maxTokens = 10, refillRate = 10.0 / 60.0)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val key = resolveKey(request)
        val limiter = selectLimiter(request)
        val (allowed, remaining) = limiter.tryConsume(key)

        response.setHeader("X-RateLimit-Limit", limiter.maxTokens.toString())
        response.setHeader("X-RateLimit-Remaining", remaining.toInt().toString())

        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}, path: {}", key, request.requestURI)
            response.status = 429
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"
            response.setHeader("Retry-After", "60")
            response.writer.write("""{"success":false,"message":"請求過於頻繁，請稍後再試"}""")
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveKey(request: HttpServletRequest): String {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth != null && auth.isAuthenticated && auth.principal != "anonymousUser") {
            return "user:${auth.name}"
        }
        return "ip:${getClientIp(request)}"
    }

    private fun getClientIp(request: HttpServletRequest): String {
        val xff = request.getHeader("X-Forwarded-For")
        if (!xff.isNullOrBlank()) return xff.split(",")[0].trim()
        val xri = request.getHeader("X-Real-IP")
        if (!xri.isNullOrBlank()) return xri.trim()
        return request.remoteAddr
    }

    private fun selectLimiter(request: HttpServletRequest): RateLimiter {
        if (request.method == "POST" && request.requestURI.matches(Regex("^/api/bookings/?$"))) {
            return bookingLimiter
        }
        return generalLimiter
    }
}
