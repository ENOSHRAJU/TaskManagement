package com.example.taskManagement.Service;

import com.example.taskManagement.Enums.RoleTypes;
import com.example.taskManagement.Exception.UserNotFound;
import com.example.taskManagement.Model.Role;
import com.example.taskManagement.Model.User;
import com.example.taskManagement.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test user");
        user.setEmail("test@example.com");
    }

    @Test
    public void hasRole_returnsTrue_whenUserHasTheRole() {
        Role role = new Role();
        role.setRole(RoleTypes.USER.name());

        boolean result = userServiceImpl.hasRole(Set.of(role), RoleTypes.USER);

        assertThat(result).isTrue();
    }

    @Test
    public void hasRole_returnsFalse_whenUserDoesNotHaveTheRole() {
        Role role = new Role();
        role.setRole(RoleTypes.USER.name());

        boolean result = userServiceImpl.hasRole(Set.of(role), RoleTypes.ADMIN);

        assertThat(result).isFalse();
    }

    @Test
    public void hasRole_returnsFalse_whenRoleSetIsEmpty() {
        boolean result = userServiceImpl.hasRole(Set.of(), RoleTypes.USER);

        assertThat(result).isFalse();
    }

    @Test
    public void findUserById_returnsUser_whenUserExists() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        User result = userServiceImpl.findUserById(user.getId());

        assertThat(result.getId()).isEqualTo(user.getId());
        assertThat(result.getName()).isEqualTo(user.getName());
    }

    @Test
    public void findUserById_throwsUserNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userServiceImpl.findUserById(user.getId()))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining(user.getId().toString());
    }

    @Test
    public void getUserByEmail_returnsUser_whenUserExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        User result = userServiceImpl.getUserByEmail(user.getEmail());

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void getUserByEmail_returnsNull_whenUserDoesNotExist() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        User result = userServiceImpl.getUserByEmail(user.getEmail());
        assertThat(result).isNull();
    }

    @Test
    public void findUserByEmail_returnsUser_whenUserExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        User result = userServiceImpl.findUserByEmail(user.getEmail());
        assertThat(result.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    public void findUserByEmail_throwsUserNotFound_whenUserDoesNotExist() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userServiceImpl.findUserByEmail(user.getEmail()))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining(user.getEmail());
    }

    @Test
    public void existsUserByEmail_returnsTrue_whenEmailExists() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);
        boolean result = userServiceImpl.existsUserByEmail(user.getEmail());
        assertThat(result).isTrue();
    }

    @Test
    public void existsUserByEmail_returnsFalse_whenEmailDoesNotExist() {
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        boolean result = userServiceImpl.existsUserByEmail(user.getEmail());
        assertThat(result).isFalse();
    }

    @Test
    public void getUserRoles_returnsAuthorityStrings_whenUserHasRoles() {
        UserDetails userDetails = mock(UserDetails.class);
        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_MANAGER")
        );
        doReturn(authorities).when(userDetails).getAuthorities();
        List<String> result = userServiceImpl.getUserRoles(userDetails);
        assertThat(result).containsExactly("ROLE_USER", "ROLE_MANAGER");
    }

    @Test
    public void getUserRoles_returnsEmptyList_whenUserHasNoRoles() {
        UserDetails userDetails = mock(UserDetails.class);
        doReturn(List.of()).when(userDetails).getAuthorities();
        List<String> result = userServiceImpl.getUserRoles(userDetails);
        assertThat(result).isEmpty();
    }

    @Test
    public void getHighestAuthRole_returnsAdmin_whenRolesContainAdmin() {
        String result = userServiceImpl.getHighestAuthRole(List.of("ROLE_USER", "ROLE_ADMIN"));
        assertThat(result).isEqualTo("ADMIN");
    }

    @Test
    public void getHighestAuthRole_returnsManager_whenRolesContainManagerButNotAdmin() {
        String result = userServiceImpl.getHighestAuthRole(List.of("ROLE_USER", "ROLE_MANAGER"));
        assertThat(result).isEqualTo("MANAGER");
    }

    @Test
    public void getHighestAuthRole_returnsUser_whenRolesContainOnlyUser() {
        String result = userServiceImpl.getHighestAuthRole(List.of("ROLE_USER"));
        assertThat(result).isEqualTo("USER");
    }

    @Test
    public void getHighestAuthRole_returnsUser_whenRolesListIsEmpty() {
        String result = userServiceImpl.getHighestAuthRole(List.of());
        assertThat(result).isEqualTo("USER");
    }

    @Test
    public void getHighestAuthRole_prioritizesAdminOverManager_whenBothPresent() {
        String result = userServiceImpl.getHighestAuthRole(List.of("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER"));
        assertThat(result).isEqualTo("ADMIN");
    }
    @Test
    public void saveUser_callsRepositorySave() {
        userServiceImpl.saveUser(user);

        verify(userRepository).save(user);
    }
}