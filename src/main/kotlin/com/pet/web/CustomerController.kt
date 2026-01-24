package com.pet.web

import com.pet.dto.CustomerDto
import com.pet.dto.response.ApiResponse
import com.pet.service.CustomerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.UUID

/**
 * 一般使用者（飼主）API
 */
@RestController
@RequestMapping("/api/customers")
class   CustomerController(
    private val customerService: CustomerService
) {

    /**
     * 取得所有使用者
     * GET /api/customers
     */
    @GetMapping
    fun getAllCustomers(): ResponseEntity<ApiResponse<List<CustomerDto>>> {
        val customers = customerService.getAllCustomers()
        return ResponseEntity.ok(ApiResponse.success(customers))
    }

    /**
     * 根據 ID 取得使用者
     * GET /api/customers/{id}
     */
    @GetMapping("/{id}")
    fun getCustomerById(@PathVariable id: UUID): ResponseEntity<ApiResponse<CustomerDto>> {
        val customer = customerService.getCustomerById(id)
        return ResponseEntity.ok(ApiResponse.success(customer))
    }

    /**
     * 根據 User ID 取得使用者
     * GET /api/customers/user/{userId}
     */
    @GetMapping("/user/{userId}")
    fun getCustomerByUserId(@PathVariable userId: UUID): ResponseEntity<ApiResponse<CustomerDto>> {
        val customer = customerService.getCustomerByUserId(userId)
        return ResponseEntity.ok(ApiResponse.success(customer))
    }
}
