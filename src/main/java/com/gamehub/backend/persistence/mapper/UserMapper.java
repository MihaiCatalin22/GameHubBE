package com.gamehub.backend.persistence.mapper;

import com.gamehub.backend.domain.Role;
import com.gamehub.backend.domain.User;
import com.gamehub.backend.dto.UserDTO;
import org.mapstruct.*;

import java.util.List;
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "jwt", ignore = true)
    UserDTO toDto(User user);

    @AfterMapping
    default void convertRolesToStrings(User user, @MappingTarget UserDTO dto) {
        List<String> roles = user.getRoles()
                .stream()
                .map(Enum::name)
                .toList();
        dto.setRole(roles);
        dto.setProfilePicture(user.getProfilePicture());
    }

    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(UserDTO userDTO);

    @AfterMapping
    default void handleRoleConversion(@MappingTarget User user, UserDTO userDTO) {
        if (userDTO.getRole() != null) {
            user.setRoles(userDTO.getRole().stream()
                    .map(String::toUpperCase)
                    .map(Role::valueOf)
                    .toList());
        }
    }
}
