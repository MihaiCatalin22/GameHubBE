package com.gamehub.backend.business.impl;

import com.gamehub.backend.dto.UserDTO;
import com.gamehub.backend.business.UserService;
import com.gamehub.backend.configuration.security.token.JwtUtil;
import com.gamehub.backend.domain.Role;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        User user = mapToEntity(userDTO);
        user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
        user = userRepository.save(user);
        return mapToDto(user);
    }
    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::mapToDto);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToDto).toList();
    }
    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    existingUser.setUsername(userDTO.getUsername());
                    existingUser.setEmail(userDTO.getEmail());
                    if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty() &&
                            !passwordEncoder.matches(userDTO.getPassword(), existingUser.getPasswordHash())) {
                        existingUser.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
                    }
                    existingUser.setDescription(userDTO.getDescription());
                    if (userDTO.getRole() != null) {
                        existingUser.setRoles(userDTO.getRole().stream().map(Role::valueOf).toList());
                    } else {
                        existingUser.setRoles(Collections.emptyList());
                    }
                    userRepository.save(existingUser);
                    return mapToDto(existingUser);
                }).orElseThrow(() -> new RuntimeException("User not found"));
    }
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    @Override
    public Optional<UserDTO> login(UserDTO userDTO) {
        return userRepository.findByUsername(userDTO.getUsername())
                .filter(user -> passwordEncoder.matches(userDTO.getPassword(), user.getPasswordHash()))
                .map(this::mapToDto);
    }

    private UserDTO mapToDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setDescription(user.getDescription());
        dto.setJwt(jwtUtil.generateToken(user.getUsername()));
        dto.setRole(user.getRoles().stream().map(Enum::name).toList());
        return dto;
    }

    private User mapToEntity(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setDescription(dto.getDescription());
        if (dto.getRole() != null) {
            user.setRoles(dto.getRole().stream().map(Role::valueOf).toList());
        } else {
            user.setRoles(new ArrayList<>());
        }
        return user;
    }
}
