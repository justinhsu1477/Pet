package com.pet.service;

import com.pet.domain.User;
import com.pet.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;


    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return null;
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, password);
            authenticationManager.authenticate(authenticationToken);
            return user;
        } else {
            return null;
        }
    }
}
