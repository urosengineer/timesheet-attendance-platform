package com.uros.timesheet.attendance.service.helper;

import com.uros.timesheet.attendance.domain.User;
import com.uros.timesheet.attendance.dto.user.UserCreateRequest;
import com.uros.timesheet.attendance.i18n.MessageUtil;
import com.uros.timesheet.attendance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserValidationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageUtil messageUtil;

    @InjectMocks
    private UserValidationService userValidationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Default stub za messageUtil da vraća ključ (ili možeš .thenReturn("msg:" + key))
        when(messageUtil.get(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void validateCreateRequest_whenUsernameMissing_throws() {
        UserCreateRequest req = new UserCreateRequest();
        req.setEmail("e@e.com");
        req.setPassword("pw");
        req.setOrganizationId(UUID.randomUUID());

        assertThatThrownBy(() -> userValidationService.validateCreateRequest(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.user.username.required");
    }

    @Test
    void validateCreateRequest_whenEmailMissing_throws() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("korisnik");
        req.setPassword("pw");
        req.setOrganizationId(UUID.randomUUID());

        assertThatThrownBy(() -> userValidationService.validateCreateRequest(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.user.email.required");
    }

    @Test
    void validateCreateRequest_whenPasswordMissing_throws() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("korisnik");
        req.setEmail("e@e.com");
        req.setOrganizationId(UUID.randomUUID());

        assertThatThrownBy(() -> userValidationService.validateCreateRequest(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.user.password.required");
    }

    @Test
    void validateCreateRequest_whenOrganizationIdMissing_throws() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("korisnik");
        req.setEmail("e@e.com");
        req.setPassword("pw");

        assertThatThrownBy(() -> userValidationService.validateCreateRequest(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.organization.id.required");
    }

    @Test
    void validateCreateRequest_whenUsernameExists_throws() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("korisnik");
        req.setEmail("e@e.com");
        req.setPassword("pw");
        req.setOrganizationId(UUID.randomUUID());

        when(userRepository.findByUsername("korisnik")).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> userValidationService.validateCreateRequest(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.user.username.exists");
    }

    @Test
    void validateCreateRequest_whenEmailExists_throws() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("korisnik");
        req.setEmail("e@e.com");
        req.setPassword("pw");
        req.setOrganizationId(UUID.randomUUID());

        when(userRepository.findByUsername("korisnik")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("e@e.com")).thenReturn(Optional.of(mock(User.class)));

        assertThatThrownBy(() -> userValidationService.validateCreateRequest(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("error.user.email.exists");
    }

    @Test
    void validateCreateRequest_whenValid_noException() {
        UserCreateRequest req = new UserCreateRequest();
        req.setUsername("korisnik");
        req.setEmail("e@e.com");
        req.setPassword("pw");
        req.setOrganizationId(UUID.randomUUID());

        when(userRepository.findByUsername("korisnik")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("e@e.com")).thenReturn(Optional.empty());

        assertThatCode(() -> userValidationService.validateCreateRequest(req))
                .doesNotThrowAnyException();
    }

    @Test
    void ensureNotDeleted_whenDeleted_throws() {
        User u = new User();
        u.markDeleted();

        when(messageUtil.get("error.user.already.deleted")).thenReturn("User is deleted!");

        assertThatThrownBy(() -> userValidationService.ensureNotDeleted(u))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User is deleted!");
    }

    @Test
    void ensureNotDeleted_whenNotDeleted_doesNothing() {
        User u = new User();
        u.setDeletedAt(null);

        assertThatCode(() -> userValidationService.ensureNotDeleted(u)).doesNotThrowAnyException();
    }

    @Test
    void ensureDeleted_whenNotDeleted_throws() {
        User u = new User();
        u.setDeletedAt(null);

        when(messageUtil.get("error.user.not.deleted")).thenReturn("User not deleted!");

        assertThatThrownBy(() -> userValidationService.ensureDeleted(u))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("User not deleted!");
    }

    @Test
    void ensureDeleted_whenDeleted_doesNothing() {
        User u = new User();
        u.markDeleted();

        assertThatCode(() -> userValidationService.ensureDeleted(u)).doesNotThrowAnyException();
    }

    @Test
    void hashPassword_addsNoopPrefix() {
        String result = userValidationService.hashPassword("lozinka");
        assertThat(result).isEqualTo("{noop}lozinka");
    }
}
