package com.springboot.employees.service;

import com.springboot.employees.domain.Employee;
import com.springboot.employees.domain.EmployeeWrapper;
import com.springboot.employees.exceptions.BadRequestException;
import com.springboot.employees.exceptions.DuplicateItemException;
import com.springboot.employees.exceptions.ItemNotFoundException;
import com.springboot.employees.persistence.EmployeeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class EmployeeService {

    @Autowired
    private EmployeeRepository repo;

    public EmployeeWrapper saveEmployee(Employee employee) {
        log.info("Inside saveEmployee method of Employee service...");
        log.info("Employee to be created : "+employee.toString());

        EmployeeWrapper savedEmployee = null;
        try{
            if (employee.getEmailAddresses().stream().allMatch(email -> email.isPrimary() == Boolean.FALSE)) {
                throw new BadRequestException("At least one emailAddress should be marked as primary!");
            }

            employee.setId(UUID.randomUUID().toString());
            employee.setCreatedAt(LocalDateTime.now());
            employee.setUpdatedAt(LocalDateTime.now());
            savedEmployee =  EmployeeWrapper.builder().employee(repo.save(employee)).isIdempotent(false).build();
        }catch (DuplicateKeyException | ParseException ex){
            Employee existingEmployee = retrieveEmployeeByFirstName(employee.getFirstName());
            if(null != existingEmployee) {
                savedEmployee = EmployeeWrapper.builder().employee(existingEmployee).isIdempotent(true).build();
            }
        }

        return savedEmployee;
    }


    public Employee retrieveEmployeeById(String id) {
        log.info("Inside retrieveEmployeeById method of Employee service...");
        log.info("Employee id to be retrieved : "+UUID.fromString(id));

        return repo.findById(id);
    }

    public Employee retrieveEmployeeByFirstName(String firstName) {
        log.info("Inside retrieveEmployeeByFirstName method of Employee service...");
        log.info("Employee firstName to be retrieved : "+firstName);

        return repo.findByFirstName(firstName);
    }

    public List<Employee> retrieveEmployeesByDepartmentName(String departmentName) {
        log.info("Inside retrieveEmployeesByDepartmentName method of Employee service...");
        log.info("Employee departmentName to be retrieved : "+departmentName);

        return repo.findByDepartmentName(departmentName);
    }


    public Employee updateEmployee(Employee updateBody) throws ParseException {
        log.info("Inside updateEmployee method of Employee service...");
        log.info("Employee id to be updated : "+updateBody.getId());

        Employee original = retrieveEmployeeById(updateBody.getId());
        if(original != null) {

            if(original.equals(updateBody)) {
                throw new DuplicateItemException("The employee details you wish to update to already exists!");
            }

            original.setFirstName(updateBody.getFirstName());
            original.setLastName(updateBody.getLastName());
            original.setDepartment(updateBody.getDepartment());
            original.setAddress(updateBody.getAddress());
            original.setUpdatedAt(LocalDateTime.now());
            original.setEmailAddresses(updateBody.getEmailAddresses());

            return repo.save(original);
        } else {
            throw new ItemNotFoundException("The employee you wish to update doesn't exist!");
        }
    }

    public void deleteEmployee(String id) {
        log.info("Inside deleteEmployee method of Employee service...");
        log.info("Employee id to be updated : "+id);

        Employee employee = retrieveEmployeeById(id);
        if (employee != null) {
            repo.delete(employee);
        } else {
            throw new ItemNotFoundException("The employee with id : "+id+" you wish to delete doesn't exist!");
        }
    }
}
