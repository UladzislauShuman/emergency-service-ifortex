package ifortex.shuman.uladzislau.authservice.service;

import ifortex.shuman.uladzislau.authservice.model.Role;
import ifortex.shuman.uladzislau.authservice.model.UserRole;

public interface RoleService {
    Role getDefaultClientRole();
    Role findByName(UserRole name);
}
