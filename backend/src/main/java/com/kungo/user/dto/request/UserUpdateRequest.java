package com.kungo.user.dto.request;

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
public class UserUpdateRequest {

    @Size(min = 2, max = 100, message = "{error.validation}")
    private String firstName;

    @Size(min = 2, max = 100, message = "{error.validation}")
    private String lastName;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "{error.validation}")
    private String phone;

    @Pattern(regexp = "^(fr|en)$", message = "{error.validation}")
    private String locale;
}
