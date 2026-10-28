package org.example.testutil;

import org.example.model.user.Role;
import java.util.Set;

public class UserRoleTestUtil {

    public static Role getRoleAdmin() {
        Role role = new Role();
        role.setId(2L);
        role.setRole(Role.RoleName.ROLE_ADMIN);

        return role;
    }

    public static Role getRoleUser() {
        Role role = new Role();
        role.setId(1L);
        role.setRole(Role.RoleName.ROLE_USER);

        return role;
    }

    public static Set<Role> getRolesUser() {
        Role role = new Role();
        Role.RoleName roleName = Role.RoleName.ROLE_USER;
        role.setId(1L);
        role.setRole(roleName);
        return Set.of(role);
    }
}
