package org.example.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserUpdatePasswordRequestDto {
    @Length(min = 8, max = 30)
    @NotBlank
    private String oldPassword;

    @Length(min = 8, max = 30)
    @NotBlank
    private String newPassword;

    @Length(min = 8, max = 30)
    @NotBlank
    private String confirmPassword;
}
