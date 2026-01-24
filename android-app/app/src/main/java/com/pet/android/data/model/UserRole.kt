package com.pet.android.data.model

/**
 * Represents the permission role of the current user.
 */
enum class UserRole(val code: String) {
    ADMIN("ADMIN"),
    SITTER("SITTER"),
    CUSTOMER("CUSTOMER"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): UserRole = when (value?.uppercase()) {
            ADMIN.code -> ADMIN
            SITTER.code -> SITTER
            CUSTOMER.code -> CUSTOMER
            else -> UNKNOWN
        }
    }
}
