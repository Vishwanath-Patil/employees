package com.springboot.employees.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Objects;

@JsonPropertyOrder({ "_links", "_embedded", "firstName", "lastName", "department", "addresses", "id", "createdAt", "updatedAt"})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "employees")
@CompoundIndex(def = "{'firstName':1,'lastName':1, 'department.id':1}", unique = true)
public class Employee extends AbstractLinkableEntity {

    @Id
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "firstName can't be empty!.")
    private String firstName;

    @NotBlank(message = "lastName can't be empty!.")
    private String lastName;

    @NotBlank(message = "Employee department details are required.")
    private Department department;

    @NotBlank(message = "At least one address is required.")
    private Address address;

    @NotEmpty(message = "At least one email is required.")
    private List<EmailAddress> emailAddresses;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return firstName.equals(employee.firstName) && lastName.equals(employee.lastName) && department.equals(employee.department) && address.equals(employee.address) && emailAddresses.equals(employee.emailAddresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, department, address, emailAddresses);
    }
}
