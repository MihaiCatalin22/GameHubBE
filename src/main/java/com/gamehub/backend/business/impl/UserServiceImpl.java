package com.gamehub.backend.business.impl;

import com.gamehub.backend.dto.UserDTO;
import com.gamehub.backend.business.UserService;
import com.gamehub.backend.configuration.security.token.JwtUtil;
import com.gamehub.backend.domain.Role;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.persistence.UserRepository;
import com.gamehub.backend.persistence.mapper.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        if (!StringUtils.hasText(userDTO.getUsername()) || !StringUtils.hasText(userDTO.getEmail()) || !StringUtils.hasText(userDTO.getPassword())) {
            throw new IllegalArgumentException("Username, email, and password must not be empty");
        }
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = prepareUserEntity(userDTO);
        user = userRepository.save(user);
        return buildUserDTOwithJwt(user);
    }
    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return Optional.ofNullable(userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + id)));
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
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
    public void updateUserProfilePicture(Long userId, String fileName) {
        userRepository.findById(userId)
                .map(user -> {
                    user.setProfilePicture(fileName);
                    return userRepository.save(user);
                }).orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }
    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }


    private User prepareUserEntity(UserDTO userDTO) {
        User user = userMapper.toEntity(userDTO);
        if (userDTO.getId() == null) {
            user.setRoles(Collections.singletonList(Role.USER));
        }
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(userDTO.getPassword()));
        }
        return user;
    }
    private UserDTO buildUserDTOwithJwt(User user) {
        UserDTO dto = userMapper.toDto(user);
        dto.setJwt(jwtUtil.generateToken(dto.getUsername()));
        return dto;
    }

    @Override
    public Optional<UserDTO> login(UserDTO userDTO) {
        return userRepository.findByUsername(userDTO.getUsername())
                .filter(user -> passwordEncoder.matches(userDTO.getPassword(), user.getPasswordHash()))
                .map(this::mapToDto);
    }

    private UserDTO mapToDto(User user) {
        UserDTO dto = userMapper.toDto(user);
        dto.setJwt(jwtUtil.generateToken(user.getUsername()));
        return dto;
    }
}
