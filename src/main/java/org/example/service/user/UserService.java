package org.example.service.user;

import org.example.dto.user.UserRegistrationRequestDto;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserUpdatePasswordRequestDto;
import org.example.dto.user.UserUpdateRequestDto;
import org.example.dto.user.UserUpdateRoleRequestDto;

public interface UserService {
    UserResponseDto registerUser(
            UserRegistrationRequestDto userRegistrationRequestDto)
            throws RuntimeException;

    UserResponseDto getInfo(String email);

    UserResponseDto update(String email,
                           UserUpdateRequestDto userUpdateRequestDto);

    void updatePassword(String email,
                        UserUpdatePasswordRequestDto userUpdatePasswordRequestDto);

    UserResponseDto updateRoles(Long id,
                                UserUpdateRoleRequestDto userUpdateRoleRequestDto);
}
