package com.pet.android.data.model

/**
 * Represents the permission role of the current user.
 */
enum class UserRole(val code: String) {
    ADMIN("ADMIN"),
    SITTER("SITTER"),
    USER("USER"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun fromString(value: String?): UserRole = when (value?.uppercase()) {
            ADMIN.code -> ADMIN
            SITTER.code -> SITTER
            USER.code -> USER
            else -> UNKNOWN
        }
    }
}
