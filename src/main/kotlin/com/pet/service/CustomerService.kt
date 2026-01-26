package com.pet.service

import com.pet.domain.Customer
import com.pet.dto.CustomerDto
import com.pet.exception.ResourceNotFoundException
import com.pet.repository.CustomerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 一般使用者（飼主）服務
 */
@Service
@Transactional(readOnly = true)
class CustomerService(
    private val customerRepository: CustomerRepository,
    private val userRepository: com.pet.repository.UserRepository
) {

    fun getAllCustomers(): List<CustomerDto> {
        // 從 Users 表找出所有角色為 CUSTOMER 的用戶
        val users = userRepository.findByRole(com.pet.domain.UserRole.CUSTOMER)
        
        return users.map { user ->
            // 嘗試取得關聯的 Customer 資料
            val customer = user.customer
            if (customer != null) {
                convertToDto(customer)
            } else {
                // 如果沒有 Customer 資料，僅回傳 User 基本資訊
                CustomerDto(
                    id = null,
                    userId = user.id,
                    username = user.username,
                    email = user.getEmail(),
                    phone = user.getPhone(),
                    name = user.getUsername() ?: "未設定", // Fallback
                    address = null,
                    emergencyContact = null,
                    emergencyPhone = null,
                    memberLevel = "BRONZE",
                    totalBookings = 0,
                    totalSpent = 0.0,
                    createdAt = null,
                    updatedAt = null
                )
            }
        }
    }

    /**
     * 根據 ID 取得使用者 (Customer ID)
     */
    fun getCustomerById(id: UUID): CustomerDto {
        val customer = customerRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("使用者", "id", id) }
        return convertToDto(customer)
    }

    /**
     * 根據 User ID 取得使用者
     */
    fun getCustomerByUserId(userId: UUID): CustomerDto {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User", "id", userId) }
            
        val customer = user.getCustomer()
        return if (customer != null) {
            convertToDto(customer)
        } else {
             CustomerDto(
                id = null,
                userId = user.getId(),
                username = user.getUsername(),
                email = user.getEmail(),
                phone = user.getPhone(),
                name = user.getUsername() ?: "未設定",
                address = null,
                emergencyContact = null,
                emergencyPhone = null,
                memberLevel = "BRONZE",
                totalBookings = 0,
                totalSpent = 0.0,
                createdAt = null,
                updatedAt = null
            )
        }
    }

    /**
     * 轉換為 DTO
     * 使用 getter 方法存取 Java Entity 的欄位（Kotlin-Java 互操作）
     */
    private fun convertToDto(customer: Customer): CustomerDto {
        val user = customer.getUser()
        return CustomerDto(
            id = customer.getId(),
            userId = user?.getId(),
            username = user?.getUsername(),
            email = user?.getEmail(),
            phone = user?.getPhone(),
            name = customer.getName(),
            address = customer.getAddress(),
            emergencyContact = customer.getEmergencyContact(),
            emergencyPhone = customer.getEmergencyPhone(),
            memberLevel = customer.getMemberLevel(),
            totalBookings = customer.getTotalBookings(),
            totalSpent = customer.getTotalSpent(),
            createdAt = customer.getCreatedAt(),
            updatedAt = customer.getUpdatedAt()
        )
    }
}
