package ifortex.shuman.uladzislau.authservice.model;

import java.util.Set;

public record FilterCriteria(Set<UserRole> roles, Set<UserStatus> statuses) {}