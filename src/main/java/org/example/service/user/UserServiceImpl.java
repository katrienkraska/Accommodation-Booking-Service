package org.example.service.user;

import lombok.RequiredArgsConstructor;
import org.example.dto.user.UserRegistrationRequestDto;
import org.example.dto.user.UserResponseDto;
import org.example.dto.user.UserUpdatePasswordRequestDto;
import org.example.dto.user.UserUpdateRequestDto;
import org.example.dto.user.UserUpdateRoleRequestDto;
import org.example.exception.EntityNotFoundException;
import org.example.exception.IncorrectPasswordException;
import org.example.exception.RegistrationException;
import org.example.mapper.UserMapper;
import org.example.model.user.Role;
import org.example.model.user.User;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Transactional
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto registerUser(UserRegistrationRequestDto userRegistrationRequestDto)
            throws RegistrationException {

        if (userRepository.existsByEmail(userRegistrationRequestDto.getEmail())) {
            throw new RegistrationException("User with email "
                    + userRegistrationRequestDto.getEmail()
                    + " already exists.");
        }

        User user = userMapper.toModel(userRegistrationRequestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role userRole = roleRepository.findByRoleName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getInfo(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));

        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto update(String email,
                                  UserUpdateRequestDto userUpdateRequestDto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));
        userMapper.setUpdateInfoToUser(user, userUpdateRequestDto);



        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void updatePassword(String email,
                               UserUpdatePasswordRequestDto userUpdatePasswordRequestDto) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by email: " + email));

        if (!passwordEncoder.matches(userUpdatePasswordRequestDto.getOldPassword(),
                user.getPassword())) {
            throw new IncorrectPasswordException("Old password is incorrect.");
        }

        if (!userUpdatePasswordRequestDto.getNewPassword()
                .equals(userUpdatePasswordRequestDto.getConfirmPassword())) {
            throw new IncorrectPasswordException(
                    "New password and confirmation do not match");
        }

        String encode = passwordEncoder.encode(userUpdatePasswordRequestDto.getNewPassword());
        user.setPassword(encode);
        userRepository.save(user);
    }

    @Override
    public UserResponseDto updateRoles(Long id,
                                       UserUpdateRoleRequestDto userUpdateRoleRequestDto) {

        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Can't find user by id: " + id));

        Role newRole = roleRepository.findByRoleName(Role.RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        user.getRoles().clear();
        user.setRoles(Set.of(newRole));

        return userMapper.toDto(user);
    }
}
