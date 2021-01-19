package com.springboot.employees.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.springboot.employees.util.JsonDateDeserializer;
import com.springboot.employees.util.JsonDateSerializer;

import java.text.ParseException;
import java.time.LocalDateTime;

public abstract class AbstractLinkableEntity {

    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    private LocalDateTime createdAt;

    @JsonSerialize(using = JsonDateSerializer.class)
    @JsonDeserialize(using = JsonDateDeserializer.class)
    private LocalDateTime updatedAt;

    public LocalDateTime getCreatedAt() throws ParseException {
        return this.createdAt == null?null: LocalDateTime.from(JsonDateSerializer.formatter.parse(JsonDateSerializer.formatter.format(createdAt)));
    }

    public LocalDateTime getUpdatedAt() throws ParseException {
        return this.updatedAt == null?null: LocalDateTime.from(JsonDateSerializer.formatter.parse(JsonDateSerializer.formatter.format(updatedAt)));
    }

    public void setCreatedAt(LocalDateTime date) throws ParseException {
        this.createdAt = date == null?LocalDateTime.now(): LocalDateTime.from(JsonDateSerializer.formatter.parse(JsonDateSerializer.formatter.format(date)));
    }

    public void setUpdatedAt(LocalDateTime date) throws ParseException {
        this.updatedAt = date == null?LocalDateTime.now(): LocalDateTime.from(JsonDateSerializer.formatter.parse(JsonDateSerializer.formatter.format(date)));
    }

    public void cleanUp() throws ParseException {
        setCreatedAt(null);
        setUpdatedAt(null);
    }

}
