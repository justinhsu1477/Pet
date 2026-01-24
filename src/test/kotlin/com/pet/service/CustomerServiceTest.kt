package com.pet.service

import com.pet.domain.Customer
import com.pet.domain.UserRole
import com.pet.domain.Users
import com.pet.exception.ResourceNotFoundException
import com.pet.repository.CustomerRepository
import com.pet.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.util.*

class CustomerServiceTest {

    private lateinit var customerService: CustomerService

    @Mock
    private lateinit var customerRepository: CustomerRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        customerService = CustomerService(customerRepository, userRepository)
    }

    @Test
    fun `getAllCustomers should return list of CustomerDto`() {
        // Arrange
        val userId1 = UUID.randomUUID()
        val user1 = Users().apply {
            id = userId1
            username = "user1"
            email = "user1@example.com"
            role = UserRole.CUSTOMER
        }
        val customer1 = Customer().apply {
            id = UUID.randomUUID()
            name = "Customer One"
            setUser(user1)
        }
        // In Users.java, customer field might not be accessible if it's private without setter in Kotlin apply
        // But we can use reflection or just assume the mock behavior if needed. 
        // Actually, CustomerService calls user.getCustomer()
        
        val user2 = Users().apply {
            id = UUID.randomUUID()
            username = "user2"
            role = UserRole.CUSTOMER
        }

        `when`(userRepository.findByRole(UserRole.CUSTOMER)).thenReturn(listOf(user1, user2))
        // Since user1 and user2 are real objects, we might need to mock them if getCustomer() is logic-heavy, 
        // but here they are just entities. However, Users.java has getCustomer() returning the private field.
        // We need to set the private field.
        
        val customerField = Users::class.java.getDeclaredField("customer")
        customerField.isAccessible = true
        customerField.set(user1, customer1)

        // Act
        val result = customerService.getAllCustomers()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Customer One", result[0].name)
        assertEquals("user1", result[0].username)
        assertEquals("未設定", result[1].name)
        assertEquals("user2", result[1].username)
    }

    @Test
    fun `getCustomerById should return CustomerDto when found`() {
        // Arrange
        val customerId = UUID.randomUUID()
        val user = Users().apply {
            id = UUID.randomUUID()
            username = "testuser"
        }
        val customer = Customer().apply {
            id = customerId
            name = "Test Customer"
            setUser(user)
        }

        `when`(customerRepository.findById(customerId)).thenReturn(Optional.of(customer))

        // Act
        val result = customerService.getCustomerById(customerId)

        // Assert
        assertNotNull(result)
        assertEquals(customerId, result.id)
        assertEquals("Test Customer", result.name)
    }

    @Test
    fun `getCustomerById should throw exception when not found`() {
        // Arrange
        val customerId = UUID.randomUUID()
        `when`(customerRepository.findById(customerId)).thenReturn(Optional.empty())

        // Act & Assert
        assertThrows<ResourceNotFoundException> {
            customerService.getCustomerById(customerId)
        }
    }

    @Test
    fun `getCustomerByUserId should return CustomerDto when user has customer data`() {
        // Arrange
        val userId = UUID.randomUUID()
        val user = Users().apply {
            id = userId
            username = "testuser"
        }
        val customer = Customer().apply {
            id = UUID.randomUUID()
            name = "Test Customer"
            setUser(user)
        }
        
        val customerField = Users::class.java.getDeclaredField("customer")
        customerField.isAccessible = true
        customerField.set(user, customer)

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // Act
        val result = customerService.getCustomerByUserId(userId)

        // Assert
        assertEquals("Test Customer", result.name)
        assertEquals(userId, result.userId)
    }

    @Test
    fun `getCustomerByUserId should return basic DTO when user has no customer data`() {
        // Arrange
        val userId = UUID.randomUUID()
        val user = Users().apply {
            id = userId
            username = "testuser"
        }

        `when`(userRepository.findById(userId)).thenReturn(Optional.of(user))

        // Act
        val result = customerService.getCustomerByUserId(userId)

        // Assert
        assertNull(result.id)
        assertEquals("testuser", result.username)
        assertEquals("未設定", result.name)
    }
}
