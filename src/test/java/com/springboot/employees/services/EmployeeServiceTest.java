package com.springboot.employees.services;

import com.springboot.employees.domain.*;
import com.springboot.employees.exceptions.DuplicateItemException;
import com.springboot.employees.exceptions.ItemNotFoundException;
import com.springboot.employees.persistence.EmployeeRepository;
import com.springboot.employees.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EmployeeServiceTest {

    @Autowired
    private EmployeeService service;

    @MockBean
    private EmployeeRepository repository;

    private Employee employee;

    private EmployeeWrapper employeeWrapper;

    @Before
    public void setUp() {
        employee = createEmployee();
        employeeWrapper = createEmployeeWrapper(employee);
    }



    @Test
    public void saveEmployeeTest() {
        when(repository.save(employee)).thenReturn(employee);

        EmployeeWrapper wrapper = service.saveEmployee(employee);
        assertEquals(wrapper.getEmployee(), employee);
        assertFalse(wrapper.isIdempotent());
        assertEquals(wrapper.getEmployee().getLastName(), employee.getLastName());
        assertEquals(wrapper.getEmployee().getDepartment(), employee.getDepartment());
        assertEquals(wrapper.getEmployee().getAddress(), employee.getAddress());
        assertEquals(wrapper.getEmployee().getEmailAddresses(), employee.getEmailAddresses());
        assertEquals(wrapper.getEmployee().getEmailAddresses().size(), employee.getEmailAddresses().size());
        assertEquals(wrapper.getEmployee().getId(), employee.getId());
    }

    @Test
    public void saveEmployeeIdempotentTest() {
        when(repository.save(employee)).thenThrow(DuplicateKeyException.class);
        when(repository.findByFirstName(employee.getFirstName())).thenReturn(employee);

        EmployeeWrapper wrapper = service.saveEmployee(employee);
        assertEquals(wrapper.getEmployee(), employee);
        assertTrue(wrapper.isIdempotent());
    }

    @Test
    public void retrieveEmployeeByIdTest() {
        when(repository.findById(anyString())).thenReturn(employee);

        Employee employeeFetched = service.retrieveEmployeeById(employee.getId());
        assertNotNull(employeeFetched);
        assertEquals(employeeFetched.getFirstName(), employee.getFirstName());
    }

    @Test
    public void retrieveEmployeeByFirstNameTest() {
        when(repository.findByFirstName(anyString())).thenReturn(employee);

        Employee employeeFetched = service.retrieveEmployeeByFirstName(employee.getFirstName());
        assertNotNull(employeeFetched);
        assertEquals(employeeFetched.getFirstName(), employee.getFirstName());
    }

    @Test
    public void retrieveEmployeesByDepartmentNameTest() {
        when(repository.findByDepartmentName(anyString())).thenReturn(Collections.singletonList(employee));

        List<Employee> employeesFetched = service.retrieveEmployeesByDepartmentName(employee.getDepartment().getName());
        assertEquals(employeesFetched.size(), 1);
        assertEquals(employeesFetched.get(0).getFirstName(), employee.getFirstName());
        assertEquals(employeesFetched.get(0).getDepartment().getName(), employee.getDepartment().getName());
    }

    @Test
    public void updateEmployeeTest() throws ParseException {
        Employee.EmployeeBuilder employeeBuilder = employee.toBuilder();
        Employee updateDetails = employeeBuilder.firstName("Vish").lastName("Rane").build();

        when(repository.findById(anyString())).thenReturn(employee);
        when(repository.save(any(Employee.class))).thenReturn(updateDetails);

        Employee updatedEmployee = service.updateEmployee(updateDetails);
        assertEquals(updatedEmployee.getFirstName(), updateDetails.getFirstName());
        assertEquals(updatedEmployee.getLastName(), updateDetails.getLastName());
        assertEquals(updatedEmployee.getAddress(), employee.getAddress());
    }

    @Test(expected = DuplicateItemException.class)
    public void updateEmployeeDuplicateTest() throws ParseException {
        Employee updateDetails = createEmployee();

        when(repository.findById(anyString())).thenReturn(employee);

        service.updateEmployee(updateDetails);
    }

    @Test(expected = ItemNotFoundException.class)
    public void nonExistentUpdateTest() throws ParseException {
        Employee updateDetails = createEmployee();

        when(repository.findById(anyString())).thenReturn(null);

        service.updateEmployee(updateDetails);
    }


    @Test
    public void deleteEmployeeTest() {
        when(repository.findById(anyString())).thenReturn(employee);

        service.deleteEmployee(employee.getId());
        verify(repository, times(1)).delete(employee);
    }


    @Test(expected = ItemNotFoundException.class)
    public void nonExistentDeleteTest() {
        when(repository.findById(anyString())).thenReturn(null);

        service.deleteEmployee(employee.getId());
        verify(repository, times(0)).delete(employee);
    }



    private EmployeeWrapper createEmployeeWrapper(Employee employee) {
        return EmployeeWrapper.builder()
                .employee(employee)
                .isIdempotent(false)
                .build();

    }
    private Employee createEmployee() {

        EmailAddress emailAddress1 = EmailAddress.builder()
                .email("abc@mailinator.com")
                .isPrimary(true)
                .build();

        EmailAddress emailAddress2 = EmailAddress.builder()
                .email("def@mailinator.com")
                .isPrimary(false)
                .build();

        Address address = Address.builder()
                .city("Hillsboro")
                .street("1189 NE 89th Street")
                .zipcode(97006)
                .state("Oregon")
                .build();

        Department department = Department.builder()
                .departmentId(101)
                .function("Technology")
                .size(5000)
                .name("NDE")
                .build();

        return employee = Employee.builder()
                .firstName("Vishwanath")
                .lastName("Patil")
                .emailAddresses(Arrays.asList(emailAddress1, emailAddress2))
                .address(address)
                .department(department)
                .id(UUID.randomUUID().toString())
                .build();
    }
}
