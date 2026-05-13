package org.example.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserUpdateRequestDto {
    @Email
    private String email;
    private String firstName;
    private String lastName;
}
