package com.pet.service

import com.pet.config.FileStorageConfig
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.util.UUID

@Service
class FileStorageService(
    private val config: FileStorageConfig
) {
    private val log = LoggerFactory.getLogger(FileStorageService::class.java)
    private lateinit var basePath: Path

    @PostConstruct
    fun init() {
        basePath = Paths.get(config.basePath).toAbsolutePath().normalize()
        Files.createDirectories(basePath)
        log.info("檔案儲存目錄初始化: {}", basePath)
    }

    /**
     * 儲存圖片到本地檔案系統
     * @return 相對路徑，例如 "2026/01/pet-uuid/20260130_abc123.jpg"
     */
    fun saveImage(imageBytes: ByteArray, petId: UUID?, messageId: String): String {
        // 驗證檔案大小
        if (imageBytes.size > config.maxFileSize) {
            throw IllegalArgumentException("檔案大小超過限制: ${imageBytes.size} > ${config.maxFileSize}")
        }

        // 建立目錄結構: {year}/{month}/{petId 或 unassigned}/
        val now = LocalDate.now()
        val subDir = if (petId != null) {
            "${now.year}/${String.format("%02d", now.monthValue)}/$petId"
        } else {
            "${now.year}/${String.format("%02d", now.monthValue)}/unassigned"
        }

        val dir = basePath.resolve(subDir)
        Files.createDirectories(dir)

        // 檔名: {timestamp}_{messageId}.jpg
        val filename = "${System.currentTimeMillis()}_${messageId}.jpg"
        val filePath = dir.resolve(filename)

        // 路徑穿越防護
        if (!filePath.normalize().startsWith(basePath)) {
            throw SecurityException("非法的檔案路徑")
        }

        Files.write(filePath, imageBytes)
        log.info("照片已儲存: {}", filePath)

        return "$subDir/$filename"
    }

    /**
     * 讀取圖片檔案
     */
    fun loadImage(relativePath: String): Path {
        val filePath = basePath.resolve(relativePath).normalize()

        // 路徑穿越防護
        if (!filePath.startsWith(basePath)) {
            throw SecurityException("非法的檔案路徑")
        }

        if (!Files.exists(filePath)) {
            throw IllegalArgumentException("檔案不存在: $relativePath")
        }

        return filePath
    }

    /**
     * 刪除圖片
     */
    fun deleteImage(relativePath: String): Boolean {
        return try {
            val filePath = basePath.resolve(relativePath).normalize()
            if (!filePath.startsWith(basePath)) {
                throw SecurityException("非法的檔案路徑")
            }
            Files.deleteIfExists(filePath)
        } catch (e: Exception) {
            log.error("刪除檔案失敗: {}", e.message)
            false
        }
    }
}
