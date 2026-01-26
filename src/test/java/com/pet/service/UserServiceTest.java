package com.pet.service;

import com.pet.domain.UserRole;
import com.pet.domain.Users;
import com.pet.dto.UserDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 測試")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UUID testUserId;
    private Users testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = new Users();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPhone("0912345678");
        testUser.setRole(UserRole.CUSTOMER);

        testUserDto = new UserDto(
                testUserId,
                "testuser",
                "test@example.com",
                "0912345678",
                "CUSTOMER"
        );
    }

    @Nested
    @DisplayName("取得所有用戶測試")
    class GetAllUsersTests {

        @Test
        @DisplayName("應該回傳所有用戶")
        void shouldReturnAllUsers() {
            // given
            given(userRepository.findAll()).willReturn(List.of(testUser));

            // when
            List<UserDto> result = userService.getAllUsers();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).username()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("當沒有用戶時應該回傳空列表")
        void shouldReturnEmptyListWhenNoUsers() {
            // given
            given(userRepository.findAll()).willReturn(List.of());

            // when
            List<UserDto> result = userService.getAllUsers();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("取得單一用戶測試")
    class GetUserByIdTests {

        @Test
        @DisplayName("應該根據 ID 取得用戶")
        void shouldGetUserById() {
            // given
            given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));

            // when
            UserDto result = userService.getUserById(testUserId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.username()).isEqualTo("testuser");
            assertThat(result.email()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("當用戶不存在時應該拋出例外")
        void shouldThrowExceptionWhenUserNotFound() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(userRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("用戶");
        }
    }

    @Nested
    @DisplayName("建立用戶測試")
    class CreateUserTests {

        @Test
        @DisplayName("應該成功建立用戶")
        void shouldCreateUser() {
            // given
            UserDto newUserDto = new UserDto(null, "newuser", "new@example.com", "0987654321", "CUSTOMER");
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(Users.class))).willReturn(testUser);

            // when
            UserDto result = userService.createUser(newUserDto, "password123");

            // then
            assertThat(result).isNotNull();
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(any(Users.class));
        }
    }

    @Nested
    @DisplayName("更新用戶測試")
    class UpdateUserTests {

        @Test
        @DisplayName("應該成功更新用戶")
        void shouldUpdateUser() {
            // given
            UserDto updateDto = new UserDto(testUserId, "updateduser", "updated@example.com", "0911111111", "SITTER");
            given(userRepository.findById(testUserId)).willReturn(Optional.of(testUser));
            given(userRepository.save(any(Users.class))).willReturn(testUser);

            // when
            UserDto result = userService.updateUser(testUserId, updateDto);

            // then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(Users.class));
        }

        @Test
        @DisplayName("當用戶不存在時應該拋出例外")
        void shouldThrowExceptionWhenUserNotFoundOnUpdate() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(userRepository.findById(unknownId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.updateUser(unknownId, testUserDto))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("刪除用戶測試")
    class DeleteUserTests {

        @Test
        @DisplayName("應該成功刪除用戶")
        void shouldDeleteUser() {
            // given
            given(userRepository.existsById(testUserId)).willReturn(true);

            // when
            userService.deleteUser(testUserId);

            // then
            verify(userRepository).deleteById(testUserId);
        }

        @Test
        @DisplayName("當用戶不存在時應該拋出例外")
        void shouldThrowExceptionWhenUserNotFoundOnDelete() {
            // given
            UUID unknownId = UUID.randomUUID();
            given(userRepository.existsById(unknownId)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.deleteUser(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
