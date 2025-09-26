package ifortex.shuman.uladzislau.authservice.dto;

import ifortex.shuman.uladzislau.authservice.model.UserRole;
import ifortex.shuman.uladzislau.authservice.model.UserSearchStatus;
import lombok.Data;

import java.util.Set;

@Data
public class UserSearchRequestDto {

    private String fullName;
    private String email;
    private String phone;

    private Set<UserRole> roles;
    private Set<UserSearchStatus> statuses;
}