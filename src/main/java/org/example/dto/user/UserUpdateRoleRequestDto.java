package org.example.dto.user;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.model.user.Role;

@Data
public class UserUpdateRoleRequestDto {
    @NotNull
    private Role.RoleName roleName;
}
