package org.example.testutil;

import java.util.HashSet;
import java.util.Set;
import org.example.dto.user.RoleTypeDto;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserUpdatePasswordRequestDto;
import org.example.dto.user.UserUpdateRequestDto;
import org.example.dto.user.UserUpdateRoleRequestDto;
import org.example.model.user.Role;
import org.example.model.user.User;

public class UserTestUtil {

    public static UserResponseDto createExpectedUserResponseDto() {

        UserResponseDto userResponseDto = new UserResponseDto();

        userResponseDto.setId(2L);
        userResponseDto.setEmail("user@example.com");
        userResponseDto.setFirstName("Kate");
        userResponseDto.setLastName("Kraska");

        RoleTypeDto roleTypeDto = new RoleTypeDto();
        roleTypeDto.setId(1L);
        roleTypeDto.setName(Role.RoleName.ROLE_USER.name());

        userResponseDto.setRoles(Set.of(roleTypeDto));

        return userResponseDto;
    }

    public static UserUpdateRequestDto createUserUpdateRequestDto() {

        UserUpdateRequestDto userUpdateRequestDto = new UserUpdateRequestDto();

        userUpdateRequestDto.setEmail("updated@example.com");
        userUpdateRequestDto.setFirstName("UpdatedName");
        userUpdateRequestDto.setLastName("UpdatedLastName");

        return userUpdateRequestDto;
    }

    public static UserUpdateRoleRequestDto createUserUpdateRoleRequestDto() {

        UserUpdateRoleRequestDto updateRoleRequestDto = new UserUpdateRoleRequestDto();

        updateRoleRequestDto.setRoleName(Role.RoleName.ROLE_ADMIN);

        return updateRoleRequestDto;
    }

    public static UserUpdatePasswordRequestDto createUserUpdatePasswordRequestDto() {

        UserUpdatePasswordRequestDto userUpdatePasswordRequestDto = new UserUpdatePasswordRequestDto();

        userUpdatePasswordRequestDto.setOldPassword("password123");
        userUpdatePasswordRequestDto.setNewPassword("newpassword123");
        userUpdatePasswordRequestDto.setConfirmPassword("newpassword123");

        return userUpdatePasswordRequestDto;
    }

    public static User getExpectedUser() {
        User user = new User();
        user.setId(2L);
        user.setEmail("user@example.com");
        user.setFirstName("Kate");
        user.setLastName("Kraska");
        user.setRoles(UserRoleTestUtil.getRolesUser());
        user.setPassword("$2a$12$Fr7derozRohomkIKBFvPJuw7WfpwghiUf6S/KGHQrAWq9YY621PV2");

        return user;
    }

    public static User getUser() {
        return buildUser(
                1L, "test@gmail.com", Role.RoleName.ROLE_USER, 1L);
    }

    public static User getAdmin() {
        return buildUser(
                99L, "admin@gmail.com", Role.RoleName.ROLE_ADMIN, 2L);
    }

    public static User getStranger() {
        return buildUser(
                3L, "stranger@gmail.com", Role.RoleName.ROLE_USER, 1L);
    }

    private static User buildUser(Long id, String email, Role.RoleName roleName, Long roleId) {
        Role role = new Role();
        role.setId(roleId);
        role.setRole(roleName);

        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password123");
        user.setRoles(new HashSet<>(Set.of(role)));
        return user;
    }
}
