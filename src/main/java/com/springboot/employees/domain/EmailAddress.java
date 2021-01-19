package com.springboot.employees.domain;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class EmailAddress {

    @NotBlank(message = "email is required!")
    private String email;

    private boolean isPrimary;
}
