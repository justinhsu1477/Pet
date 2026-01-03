package com.pet.service;

import com.pet.domain.User;
import com.pet.dto.UserDto;
import com.pet.exception.ResourceNotFoundException;
import com.pet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用戶", "id", id));
        return convertToDto(user);
    }

    public UserDto createUser(UserDto userDto, String password) {
        User user = convertToEntity(userDto);
        user.setPassword(passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }

    public UserDto updateUser(UUID id, UserDto userDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用戶", "id", id));

        existingUser.setUsername(userDto.username());
        existingUser.setEmail(userDto.email());
        existingUser.setPhone(userDto.phone());
        existingUser.setRole(userDto.role());

        User updatedUser = userRepository.save(existingUser);
        return convertToDto(updatedUser);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("用戶", "id", id);
        }
        userRepository.deleteById(id);
    }

    private UserDto convertToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }

    private User convertToEntity(UserDto dto) {
        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPhone(dto.phone());
        user.setRole(dto.role());
        return user;
    }
}
