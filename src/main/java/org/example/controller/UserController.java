package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserUpdatePasswordRequestDto;
import org.example.dto.user.UserUpdateRequestDto;
import org.example.dto.user.UserUpdateRoleRequestDto;
import org.example.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Management",
        description = "Endpoints for managing user registration, "
                + "authentication, and profile information")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(
            summary = "Update user role by ID",
            description = "Allows an ADMIN to update the role of an existing user by their ID. "
                    + "Requires a valid role value in the request body.")
    public UserResponseDto updateRoles(@PathVariable Long id,
                                       @RequestBody @Valid UserUpdateRoleRequestDto userUpdateRoleRequestDto) {
        return userService.updateRoles(id, userUpdateRoleRequestDto);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(
            summary = "Retrieve authenticated user details",
            description = "Returns the profile information of the currently authenticated user, "
                    + "including personal data and assigned roles.")
    public UserResponseDto getInfo(Authentication authentication) {
        return userService.getInfo(authentication.getName());
    }

    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PatchMapping("/me")
    @Operation(
            summary = "Update current user profile",
            description = "Allows the authenticated user to update their profile information, "
                    + "including email, first name, and last name. ")
    public UserResponseDto update(Authentication authentication,
                                  @RequestBody @Valid UserUpdateRequestDto userUpdateRequestDto) {
        return userService.update(authentication.getName(), userUpdateRequestDto);
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @Operation(summary = "Update user password",
            description = "Allows an authenticated user with the CUSTOMER role "
                    + "to update their account password. The request must include "
                    + "the current password, a new password, and a confirmation of the "
                    + "new password.")
    public ResponseEntity<String> updatePassword(Authentication authentication,
                                                 @RequestBody @Valid
                                                 UserUpdatePasswordRequestDto userUpdatePasswordRequestDto) {
        userService.updatePassword(authentication.getName(), userUpdatePasswordRequestDto);

        return ResponseEntity.ok("Password updated successfully");
    }
}
