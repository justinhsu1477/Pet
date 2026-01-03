package com.pet.service;

import com.pet.domain.Users;
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
        Users users = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用戶", "id", id));
        return convertToDto(users);
    }

    public UserDto createUser(UserDto userDto, String password) {
        Users users = convertToEntity(userDto);
        users.setPassword(passwordEncoder.encode(password));
        Users savedUsers = userRepository.save(users);
        return convertToDto(savedUsers);
    }

    public UserDto updateUser(UUID id, UserDto userDto) {
        Users existingUsers = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("用戶", "id", id));

        existingUsers.setUsername(userDto.username());
        existingUsers.setEmail(userDto.email());
        existingUsers.setPhone(userDto.phone());
        existingUsers.setRole(userDto.role());

        Users updatedUsers = userRepository.save(existingUsers);
        return convertToDto(updatedUsers);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("用戶", "id", id);
        }
        userRepository.deleteById(id);
    }

    private UserDto convertToDto(Users users) {
        return new UserDto(
                users.getId(),
                users.getUsername(),
                users.getEmail(),
                users.getPhone(),
                users.getRole()
        );
    }

    private Users convertToEntity(UserDto dto) {
        Users users = new Users();
        users.setUsername(dto.username());
        users.setEmail(dto.email());
        users.setPhone(dto.phone());
        users.setRole(dto.role());
        return users;
    }
}
