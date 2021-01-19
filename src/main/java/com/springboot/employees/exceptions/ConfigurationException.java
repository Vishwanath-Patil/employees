package com.springboot.employees.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class ConfigurationException  extends RuntimeException {
    public ConfigurationException(String msg) {
        super(msg);
    }


}

