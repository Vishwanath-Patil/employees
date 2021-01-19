package com.springboot.employees.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeWrapper {

    private Employee employee;
    private boolean isIdempotent;
}
