package ifortex.shuman.uladzislau.authservice.paramedic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KycApprovalRequestDto {

    @NotBlank(message = "New work email cannot be blank.")
    @Email
    private String workEmail;

    @NotBlank(message = "Password for the work email cannot be blank.")
    private String workEmailPassword;
}