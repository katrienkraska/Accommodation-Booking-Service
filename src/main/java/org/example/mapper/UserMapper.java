package org.example.mapper;

import org.example.dto.user.UserRegistrationRequestDto;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserUpdateRequestDto;
import org.example.dto.user.RoleTypeDto;
import org.example.model.user.Role;
import org.example.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toModel(UserRegistrationRequestDto requestDto);

    UserResponseDto toDto(User user);

    @Mapping(target = "name", expression = "java(role.getRole().name())")
    RoleTypeDto toRoleDto(Role role);

    void setUpdateInfoToUser(@MappingTarget User user,
                             UserUpdateRequestDto userUpdateRequestDto);
}
