package com.springboot.employees.persistence;

import com.springboot.employees.domain.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends MongoRepository<Employee, UUID> {
    Employee findByFirstName(String firstName);

    Employee findById(String id);

    List<Employee> findByDepartmentName(String departmentName);
}
