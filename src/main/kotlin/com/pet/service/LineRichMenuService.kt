package com.pet.service

import com.pet.config.LineMessagingConfig
import com.pet.domain.UserRole
import com.pet.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO

/**
 * LINE Rich Menu 角色分流服務
 *
 * 依用戶角色顯示不同 Rich Menu：
 * - SITTER:   「📷 上傳照片」+「📋 我的預約」
 * - CUSTOMER: 「🐾 我的寵物」+「📋 我的預約」
 * - DEFAULT:  「🔗 前往登入」（未綁定帳號）
 *
 * 使用 per-user Rich Menu 機制，優先於 default，即時生效
 */
@Service
class LineRichMenuService(
    private val config: LineMessagingConfig,
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(LineRichMenuService::class.java)
    private val restTemplate = RestTemplate()

    // 儲存已建立的 richMenuId（key: "sitter" / "customer" / "default"）
    private val menuIdMap = ConcurrentHashMap<String, String>()

    companion object {
        private const val RICHMENU_API = "https://api.line.me/v2/bot/richmenu"
        private const val RICHMENU_CONTENT_API = "https://api-data.line.me/v2/bot/richmenu/%s/content"
        private const val RICHMENU_DEFAULT_API = "https://api.line.me/v2/bot/user/all/richmenu/%s"
        private const val RICHMENU_USER_API = "https://api.line.me/v2/bot/user/%s/richmenu/%s"
        private const val RICHMENU_LIST_API = "https://api.line.me/v2/bot/richmenu/list"

        private const val MENU_NAME_SITTER = "PetSitter-SitterMenu"
        private const val MENU_NAME_CUSTOMER = "PetSitter-CustomerMenu"
        private const val MENU_NAME_DEFAULT = "PetSitter-DefaultMenu"

        private const val WIDTH = 2500
        private const val HEIGHT = 843
    }

    /**
     * 應用啟動時自動設置所有 Rich Menu
     */
    @EventListener(ApplicationReadyEvent::class)
    fun setupOnStartup() {
        if (!config.isConfigured()) {
            log.info("LINE 未設定，跳過 Rich Menu 設置")
            return
        }
        try {
            setupAllMenus()
            assignMenusToExistingUsers()
        } catch (e: Exception) {
            log.warn("Rich Menu 自動設置失敗（不影響主功能）: {}", e.message)
        }
    }

    /**
     * 刪除所有由本系統建立的 Rich Menu 並重建
     * 用於 URL 變更時（例如 ngrok 重啟）
     */
    fun recreateAllMenus() {
        log.info("重建所有 Rich Menu...")
        deleteAllMenus()
        setupAllMenus()
        assignMenusToExistingUsers()
        log.info("Rich Menu 重建完成")
    }

    /**
     * 刪除所有由本系統建立的 Rich Menu
     */
    fun deleteAllMenus() {
        val existingMenus = listRichMenus()
        val ourMenuNames = setOf(MENU_NAME_DEFAULT, MENU_NAME_SITTER, MENU_NAME_CUSTOMER)

        existingMenus.filter { it["name"] in ourMenuNames }.forEach { menu ->
            val id = menu["richMenuId"] as? String ?: return@forEach
            try {
                val headers = createAuthHeaders()
                val entity = HttpEntity<String>(headers)
                restTemplate.exchange("$RICHMENU_API/$id", HttpMethod.DELETE, entity, String::class.java)
                log.info("已刪除 Rich Menu: name={}, id={}", menu["name"], id)
            } catch (e: Exception) {
                log.warn("刪除 Rich Menu 失敗: id={}, error={}", id, e.message)
            }
        }
        menuIdMap.clear()
    }

    /**
     * 建立 3 個 Rich Menu（冪等操作）
     */
    fun setupAllMenus() {
        val existingMenus = listRichMenus()

        // Default Menu
        setupMenu(MENU_NAME_DEFAULT, "default", existingMenus) {
            createDefaultMenu()
        }

        // Sitter Menu
        setupMenu(MENU_NAME_SITTER, "sitter", existingMenus) {
            createSitterMenu()
        }

        // Customer Menu
        setupMenu(MENU_NAME_CUSTOMER, "customer", existingMenus) {
            createCustomerMenu()
        }

        // 設 default menu 為全域預設
        menuIdMap["default"]?.let { setDefaultRichMenu(it) }

        log.info("Rich Menu 設置完成: default={}, sitter={}, customer={}",
            menuIdMap["default"], menuIdMap["sitter"], menuIdMap["customer"])
    }

    /**
     * 依角色綁定 per-user Rich Menu
     */
    fun assignMenuToUser(lineUserId: String, role: UserRole) {
        val menuKey = when (role) {
            UserRole.SITTER -> "sitter"
            UserRole.CUSTOMER -> "customer"
            UserRole.ADMIN -> "sitter" // admin 也可以看保母功能
        }

        val richMenuId = menuIdMap[menuKey]
        if (richMenuId == null) {
            log.warn("尚未建立 {} Rich Menu，無法綁定", menuKey)
            return
        }

        try {
            val url = String.format(RICHMENU_USER_API, lineUserId, richMenuId)
            val headers = createAuthHeaders()
            val entity = HttpEntity<String>(headers)
            restTemplate.postForEntity(url, entity, String::class.java)
            log.info("已綁定 Rich Menu: lineUserId={}, role={}, menuId={}", lineUserId, role, richMenuId)
        } catch (e: Exception) {
            log.error("綁定 Rich Menu 失敗: lineUserId={}, error={}", lineUserId, e.message)
        }
    }

    /**
     * 解除用戶的 per-user Rich Menu（回到 default）
     */
    fun removeUserMenu(lineUserId: String) {
        try {
            val url = "https://api.line.me/v2/bot/user/$lineUserId/richmenu"
            val headers = createAuthHeaders()
            val entity = HttpEntity<String>(headers)
            restTemplate.exchange(url, HttpMethod.DELETE, entity, String::class.java)
            log.info("已解除 Rich Menu: lineUserId={}", lineUserId)
        } catch (e: Exception) {
            log.debug("解除 Rich Menu 失敗（可能未綁定）: {}", e.message)
        }
    }

    // ==================== 內部方法 ====================

    /**
     * 啟動時批次綁定已註冊用戶
     */
    private fun assignMenusToExistingUsers() {
        val usersWithLine = userRepository.findAll().filter {
            it.getLineUserId() != null && it.getLineUserId().isNotBlank()
        }

        if (usersWithLine.isEmpty()) {
            log.info("無已綁定 LINE 的用戶，跳過批次綁定")
            return
        }

        var count = 0
        usersWithLine.forEach { user ->
            try {
                assignMenuToUser(user.getLineUserId(), user.getRole())
                count++
            } catch (e: Exception) {
                log.debug("批次綁定失敗: userId={}, error={}", user.getId(), e.message)
            }
        }
        log.info("批次綁定 Rich Menu 完成: {}/{} 位用戶", count, usersWithLine.size)
    }

    /**
     * 建立或復用既有 Menu
     */
    @Suppress("UNCHECKED_CAST")
    private fun setupMenu(
        menuName: String,
        key: String,
        existingMenus: List<Map<String, Any>>,
        creator: () -> Pair<Map<String, Any>, () -> ByteArray>
    ) {
        // 檢查是否已存在
        val existing = existingMenus.find { it["name"] == menuName }
        if (existing != null) {
            menuIdMap[key] = existing["richMenuId"] as String
            log.info("Rich Menu 已存在: name={}, id={}", menuName, existing["richMenuId"])
            return
        }

        // 建立新 Menu
        val (menuBody, imageGenerator) = creator()
        val richMenuId = createRichMenu(menuBody)
        uploadRichMenuImage(richMenuId, imageGenerator())
        menuIdMap[key] = richMenuId
        log.info("Rich Menu 建立成功: name={}, id={}", menuName, richMenuId)
    }

    // ==================== Menu 定義 ====================

    private fun createDefaultMenu(): Pair<Map<String, Any>, () -> ByteArray> {
        val frontendUrl = config.getFrontendUrl() ?: "http://localhost:3000"

        val body = mapOf(
            "size" to mapOf("width" to WIDTH, "height" to HEIGHT),
            "selected" to true,
            "name" to MENU_NAME_DEFAULT,
            "chatBarText" to "功能選單",
            "areas" to listOf(
                mapOf(
                    "bounds" to mapOf("x" to 0, "y" to 0, "width" to WIDTH, "height" to HEIGHT),
                    "action" to mapOf(
                        "type" to "uri",
                        "label" to "前往登入",
                        "uri" to "$frontendUrl/line-callback.html"
                    )
                )
            )
        )

        return Pair(body) {
            generateSingleColumnImage("Login / Register", Color(33, 150, 243))
        }
    }

    private fun createSitterMenu(): Pair<Map<String, Any>, () -> ByteArray> {
        val frontendUrl = config.getFrontendUrl() ?: "http://localhost:3000"

        val body = mapOf(
            "size" to mapOf("width" to WIDTH, "height" to HEIGHT),
            "selected" to true,
            "name" to MENU_NAME_SITTER,
            "chatBarText" to "保母選單",
            "areas" to listOf(
                // 左半：上傳照片（postback）
                mapOf(
                    "bounds" to mapOf("x" to 0, "y" to 0, "width" to WIDTH / 2, "height" to HEIGHT),
                    "action" to mapOf(
                        "type" to "postback",
                        "data" to "action=upload_photo",
                        "displayText" to "📷 上傳照片"
                    )
                ),
                // 右半：我的預約（URI）
                mapOf(
                    "bounds" to mapOf("x" to WIDTH / 2, "y" to 0, "width" to WIDTH / 2, "height" to HEIGHT),
                    "action" to mapOf(
                        "type" to "uri",
                        "label" to "我的預約",
                        "uri" to "$frontendUrl/bookings"
                    )
                )
            )
        )

        return Pair(body) {
            generateTwoColumnImage(
                "Upload Photo", "My Bookings",
                Color(46, 125, 50), Color(255, 152, 0)
            )
        }
    }

    private fun createCustomerMenu(): Pair<Map<String, Any>, () -> ByteArray> {
        val frontendUrl = config.getFrontendUrl() ?: "http://localhost:3000"

        val body = mapOf(
            "size" to mapOf("width" to WIDTH, "height" to HEIGHT),
            "selected" to true,
            "name" to MENU_NAME_CUSTOMER,
            "chatBarText" to "飼主選單",
            "areas" to listOf(
                // 左半：我的寵物（URI）
                mapOf(
                    "bounds" to mapOf("x" to 0, "y" to 0, "width" to WIDTH / 2, "height" to HEIGHT),
                    "action" to mapOf(
                        "type" to "uri",
                        "label" to "我的寵物",
                        "uri" to "$frontendUrl/pets"
                    )
                ),
                // 右半：我的預約（URI）
                mapOf(
                    "bounds" to mapOf("x" to WIDTH / 2, "y" to 0, "width" to WIDTH / 2, "height" to HEIGHT),
                    "action" to mapOf(
                        "type" to "uri",
                        "label" to "我的預約",
                        "uri" to "$frontendUrl/bookings"
                    )
                )
            )
        )

        return Pair(body) {
            generateTwoColumnImage(
                "My Pets", "My Bookings",
                Color(156, 39, 176), Color(255, 152, 0)
            )
        }
    }

    // ==================== LINE API 呼叫 ====================

    @Suppress("UNCHECKED_CAST")
    private fun listRichMenus(): List<Map<String, Any>> {
        return try {
            val headers = createAuthHeaders()
            val entity = HttpEntity<String>(headers)
            val response = restTemplate.exchange(RICHMENU_LIST_API, HttpMethod.GET, entity, Map::class.java)
            (response.body?.get("richmenus") as? List<Map<String, Any>>) ?: emptyList()
        } catch (e: Exception) {
            log.debug("查詢 Rich Menu 列表失敗: {}", e.message)
            emptyList()
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun createRichMenu(body: Map<String, Any>): String {
        val headers = createAuthHeaders()
        val entity = HttpEntity(body, headers)
        val response = restTemplate.postForEntity(RICHMENU_API, entity, Map::class.java)
        return (response.body?.get("richMenuId") as? String)
            ?: throw IllegalStateException("Rich Menu 建立失敗：無 richMenuId")
    }

    private fun uploadRichMenuImage(richMenuId: String, imageBytes: ByteArray) {
        val url = String.format(RICHMENU_CONTENT_API, richMenuId)
        val headers = HttpHeaders()
        headers.setBearerAuth(config.getChannelToken())
        headers.contentType = MediaType.IMAGE_PNG
        val entity = HttpEntity(imageBytes, headers)
        restTemplate.postForEntity(url, entity, String::class.java)
    }

    private fun setDefaultRichMenu(richMenuId: String) {
        val url = String.format(RICHMENU_DEFAULT_API, richMenuId)
        val headers = createAuthHeaders()
        val entity = HttpEntity<String>(headers)
        restTemplate.postForEntity(url, entity, String::class.java)
        log.info("已設定預設 Rich Menu: {}", richMenuId)
    }

    private fun createAuthHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.setBearerAuth(config.getChannelToken())
        return headers
    }

    // ==================== 圖片生成 ====================

    /**
     * 生成單欄 Rich Menu 圖片（Default Menu 用）
     */
    private fun generateSingleColumnImage(text: String, bgColor: Color): ByteArray {
        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        // 漸層背景
        g.paint = GradientPaint(0f, 0f, bgColor, WIDTH.toFloat(), HEIGHT.toFloat(), bgColor.darker())
        g.fillRect(0, 0, WIDTH, HEIGHT)

        // 圖示圓形
        g.color = Color(255, 255, 255, 50)
        g.fillOval(WIDTH / 2 - 100, 180, 200, 200)

        // 主文字
        g.color = Color.WHITE
        g.font = Font("SansSerif", Font.BOLD, 90)
        val metrics = g.fontMetrics
        val textX = (WIDTH - metrics.stringWidth(text)) / 2
        g.drawString(text, textX, 500)

        // 副文字
        g.font = Font("SansSerif", Font.PLAIN, 45)
        val subText = "Tap to continue"
        val subMetrics = g.fontMetrics
        g.color = Color(255, 255, 255, 180)
        g.drawString(subText, (WIDTH - subMetrics.stringWidth(subText)) / 2, 600)

        g.dispose()
        return imageToBytes(image)
    }

    /**
     * 生成雙欄 Rich Menu 圖片（Sitter / Customer Menu 用）
     */
    private fun generateTwoColumnImage(
        leftText: String, rightText: String,
        leftColor: Color, rightColor: Color
    ): ByteArray {
        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        val halfW = WIDTH / 2

        // 左半漸層
        g.paint = GradientPaint(0f, 0f, leftColor, halfW.toFloat(), HEIGHT.toFloat(), leftColor.darker())
        g.fillRect(0, 0, halfW, HEIGHT)

        // 右半漸層
        g.paint = GradientPaint(halfW.toFloat(), 0f, rightColor, WIDTH.toFloat(), HEIGHT.toFloat(), rightColor.darker())
        g.fillRect(halfW, 0, halfW, HEIGHT)

        // 中間分隔線
        g.color = Color(255, 255, 255, 80)
        g.fillRect(halfW - 2, 50, 4, HEIGHT - 100)

        // 左半文字
        drawColumnText(g, leftText, 0, halfW)

        // 右半文字
        drawColumnText(g, rightText, halfW, halfW)

        g.dispose()
        return imageToBytes(image)
    }

    private fun drawColumnText(g: java.awt.Graphics2D, text: String, startX: Int, colWidth: Int) {
        val centerX = startX + colWidth / 2

        // 圖示圓形
        g.color = Color(255, 255, 255, 50)
        g.fillOval(centerX - 80, 200, 160, 160)

        // 主文字
        g.color = Color.WHITE
        g.font = Font("SansSerif", Font.BOLD, 70)
        val metrics = g.fontMetrics
        val textX = centerX - metrics.stringWidth(text) / 2
        g.drawString(text, textX, 500)

        // 底部提示
        g.font = Font("SansSerif", Font.PLAIN, 36)
        val tap = "[ Tap ]"
        val tapMetrics = g.fontMetrics
        g.color = Color(255, 255, 255, 140)
        g.drawString(tap, centerX - tapMetrics.stringWidth(tap) / 2, 620)
    }

    private fun imageToBytes(image: BufferedImage): ByteArray {
        val out = ByteArrayOutputStream()
        ImageIO.write(image, "png", out)
        return out.toByteArray()
    }
}
