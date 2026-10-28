package org.example.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.Optional;
import java.util.Set;
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
import org.example.testutil.UserRoleTestUtil;
import org.example.testutil.UserTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String EMAIL = "test@gmail.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponseDto userResponseDto;
    private Role roleUser;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        user = UserTestUtil.getUser();
        userResponseDto = UserTestUtil.createExpectedUserResponseDto();
        roleUser = UserRoleTestUtil.getRoleUser();
        roleAdmin = UserRoleTestUtil.getRoleAdmin();
    }

    @Test
    @DisplayName("registerUser: valid data -> encodes password, assigns ROLE_USER, saves user")
    void registerUser_ValidData_ShouldReturnUserResponseDto() throws Exception {
        UserRegistrationRequestDto requestDto = buildRegistrationRequest();
        User newUser = new User();
        newUser.setEmail(EMAIL);
        newUser.setPassword("password123");

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(newUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByRoleName(Role.RoleName.ROLE_USER))
                .thenReturn(Optional.of(roleUser));
        when(userMapper.toDto(newUser)).thenReturn(userResponseDto);

        UserResponseDto actual = userService.registerUser(requestDto);

        assertSame(userResponseDto, actual);
        assertEquals("encodedPassword", newUser.getPassword());
        assertEquals(Set.of(roleUser), newUser.getRoles());
        verify(userRepository).save(newUser);
    }

    @Test
    @DisplayName("registerUser: email already exists -> RegistrationException")
    void registerUser_EmailExists_ShouldThrowException() {
        UserRegistrationRequestDto requestDto = buildRegistrationRequest();
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        RegistrationException exception = assertThrows(RegistrationException.class,
                () -> userService.registerUser(requestDto));

        assertEquals("User with email " + EMAIL + " already exists.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("registerUser: ROLE_USER not found -> EntityNotFoundException")
    void registerUser_RoleNotFound_ShouldThrowException() {
        UserRegistrationRequestDto requestDto = buildRegistrationRequest();
        User newUser = new User();
        newUser.setPassword("password123");

        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(userMapper.toModel(requestDto)).thenReturn(newUser);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(roleRepository.findByRoleName(Role.RoleName.ROLE_USER))
                .thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.registerUser(requestDto));

        assertEquals("Role not found", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("getInfo: existing email -> returns user dto")
    void getInfo_ExistingEmail_ShouldReturnUserResponseDto() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto actual = userService.getInfo(EMAIL);

        assertSame(userResponseDto, actual);
    }

    @Test
    @DisplayName("getInfo: unknown email -> EntityNotFoundException")
    void getInfo_UnknownEmail_ShouldThrowException() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.getInfo(EMAIL));

        assertEquals("Can't find user by email: " + EMAIL, exception.getMessage());
    }

    @Test
    @DisplayName("update: existing user -> applies changes, saves and returns dto")
    void update_ExistingUser_ShouldReturnUpdatedDto() {
        UserUpdateRequestDto updateRequestDto = UserTestUtil.createUserUpdateRequestDto();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto actual = userService.update(EMAIL, updateRequestDto);

        assertSame(userResponseDto, actual);
        verify(userMapper).setUpdateInfoToUser(user, updateRequestDto);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updatePassword: valid data -> saves user with encoded new password")
    void updatePassword_ValidData_ShouldSaveEncodedPassword() {
        UserUpdatePasswordRequestDto request = UserTestUtil.createUserUpdatePasswordRequestDto();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
                .thenReturn(true);
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

        userService.updatePassword(EMAIL, request);

        assertEquals("encodedNewPassword", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updatePassword: wrong old password -> IncorrectPasswordException")
    void updatePassword_WrongOldPassword_ShouldThrowException() {
        UserUpdatePasswordRequestDto requestDto = UserTestUtil.createUserUpdatePasswordRequestDto();

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(requestDto.getOldPassword(), user.getPassword()))
                .thenReturn(false);

        IncorrectPasswordException exception = assertThrows(IncorrectPasswordException.class,
                () -> userService.updatePassword(EMAIL, requestDto));

        assertEquals("Old password is incorrect.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updatePassword: confirmation does not match -> IncorrectPasswordException")
    void updatePassword_ConfirmationMismatch_ShouldThrowException() {
        UserUpdatePasswordRequestDto request = UserTestUtil.createUserUpdatePasswordRequestDto();
        request.setConfirmPassword("differentPassword123");

        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getOldPassword(), user.getPassword()))
                .thenReturn(true);

        IncorrectPasswordException exception = assertThrows(IncorrectPasswordException.class,
                () -> userService.updatePassword(EMAIL, request));

        assertEquals("New password and confirmation do not match", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("updateRoles: existing user -> replaces roles with ROLE_ADMIN")
    void updateRoles_ExistingUser_ShouldReplaceRoles() {
        UserUpdateRoleRequestDto requestDto = UserTestUtil.createUserUpdateRoleRequestDto();

        when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName(Role.RoleName.ROLE_ADMIN))
                .thenReturn(Optional.of(roleAdmin));
        when(userMapper.toDto(user)).thenReturn(userResponseDto);

        UserResponseDto actual = userService.updateRoles(1L, requestDto);

        assertSame(userResponseDto, actual);
        assertEquals(Set.of(roleAdmin), user.getRoles());
    }

    @Test
    @DisplayName("updateRoles: user not found -> EntityNotFoundException")
    void updateRoles_UserNotFound_ShouldThrowException() {
        UserUpdateRoleRequestDto requestDto = UserTestUtil.createUserUpdateRoleRequestDto();

        when(userRepository.findUserById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> userService.updateRoles(99L, requestDto));

        assertEquals("Can't find user by id: 99", exception.getMessage());
    }

    private UserRegistrationRequestDto buildRegistrationRequest() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto();
        requestDto.setEmail(EMAIL);
        requestDto.setPassword("password123");
        requestDto.setRepeatPassword("password123");
        requestDto.setFirstName("Test");
        requestDto.setLastName("User");
        return requestDto;
    }
}
