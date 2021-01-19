package com.springboot.employees.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Department {

    @NotBlank(message = "Department id is required.")
    private int departmentId;

    @NotBlank(message = "Department name is required.")
    private String name;

    private int size;

    @NotBlank(message = "Department function is required.")
    private String function;

}
