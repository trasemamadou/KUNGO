package com.kungo.auth;

import com.kungo.user.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Email(message = "{error.validation}")
    private String email;

    @NotBlank
    @Size(min = 8, max = 72, message = "{error.validation}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "{error.validation}")
    private String password;

    @NotBlank
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank
    @Size(min = 2, max = 100)
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "{error.validation}")
    private String phone;

    @NotNull
    private Role role; // CLIENT ou PROFESSIONNEL uniquement (validé en service)
}
