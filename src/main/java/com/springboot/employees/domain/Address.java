package com.springboot.employees.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
public class Address {

    @NotBlank(message = "City is required.")
    private String city;

    private String street;

    private int zipcode;

    @NotBlank(message = "State is required.")
    private String state;

}
