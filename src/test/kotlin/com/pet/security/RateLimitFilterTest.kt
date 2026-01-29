package com.pet.security

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder

class RateLimitFilterTest {

    private lateinit var filter: RateLimitFilter

    @Mock
    private lateinit var filterChain: FilterChain

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        filter = RateLimitFilter()
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should allow normal requests and set rate limit headers`() {
        val request = MockHttpServletRequest("GET", "/api/pets")
        request.remoteAddr = "192.168.1.1"
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        assertNotNull(response.getHeader("X-RateLimit-Limit"))
        assertNotNull(response.getHeader("X-RateLimit-Remaining"))
    }

    @Test
    fun `should return 429 when rate limit exceeded`() {
        val uniqueIp = "10.99.99.${System.nanoTime() % 256}"
        repeat(60) {
            val req = MockHttpServletRequest("GET", "/api/pets")
            req.remoteAddr = uniqueIp
            filter.doFilter(req, MockHttpServletResponse(), filterChain)
        }

        val request = MockHttpServletRequest("GET", "/api/pets")
        request.remoteAddr = uniqueIp
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        assertEquals(429, response.status)
        assertEquals("60", response.getHeader("Retry-After"))
    }

    @Test
    fun `should use stricter limit for POST booking endpoint`() {
        val uniqueIp = "10.88.88.${System.nanoTime() % 256}"
        repeat(10) {
            val req = MockHttpServletRequest("POST", "/api/bookings")
            req.remoteAddr = uniqueIp
            filter.doFilter(req, MockHttpServletResponse(), filterChain)
        }

        val request = MockHttpServletRequest("POST", "/api/bookings")
        request.remoteAddr = uniqueIp
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        assertEquals(429, response.status)
    }

    @Test
    fun `should use userId as key when authenticated`() {
        val auth = UsernamePasswordAuthenticationToken("testuser123", null, emptyList())
        SecurityContextHolder.getContext().authentication = auth

        val request = MockHttpServletRequest("GET", "/api/pets")
        request.remoteAddr = "127.0.0.1"
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
        assertEquals("60", response.getHeader("X-RateLimit-Limit"))

        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should extract client IP from X-Forwarded-For header`() {
        // Use a unique IP so no collision with other tests
        val request = MockHttpServletRequest("GET", "/api/pets")
        request.addHeader("X-Forwarded-For", "203.0.113.50, 70.41.3.18")
        request.remoteAddr = "127.0.0.1"
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        verify(filterChain).doFilter(request, response)
    }

    @Test
    fun `should return JSON error body when rate limited`() {
        val uniqueIp = "10.77.77.${System.nanoTime() % 256}"
        repeat(60) {
            val req = MockHttpServletRequest("GET", "/api/pets")
            req.remoteAddr = uniqueIp
            filter.doFilter(req, MockHttpServletResponse(), filterChain)
        }

        val request = MockHttpServletRequest("GET", "/api/pets")
        request.remoteAddr = uniqueIp
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        assertEquals(429, response.status)
        assertTrue(response.contentAsString.contains("success"))
        assertEquals("application/json", response.contentType)
    }
}
