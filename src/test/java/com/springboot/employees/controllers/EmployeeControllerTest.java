package com.springboot.employees.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.employees.domain.*;
import com.springboot.employees.exceptions.ItemNotFoundException;
import com.springboot.employees.persistence.EmployeeRepository;
import com.springboot.employees.service.EmployeeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc( addFilters = false)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeRepository repository;

    @MockBean
    private EmployeeService service;

    private Employee employee;

    private JacksonTester<Employee> jsonTester;

    @Before
    public void setup() throws ParseException {
        JacksonTester.initFields(this, objectMapper);
        employee = createEmployee();
    }

    @Test
    public void createTest() throws Exception {
        final String employeeJSON = jsonTester.write(employee).getJson();
        EmployeeWrapper employeeWrapper = EmployeeWrapper.builder()
                .employee(employee)
                .isIdempotent(false)
                .build();

        when(service.saveEmployee(Mockito.any())).thenReturn(employeeWrapper);
        MvcResult result = mvc.perform(post("/employees")
                .content(employeeJSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn();

        Employee createdEmployee = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<Employee>() {});
        assert (createdEmployee.getFirstName().equalsIgnoreCase(employee.getFirstName()));
        assert (createdEmployee.getLastName().equalsIgnoreCase(employee.getLastName()));
    }

    @Test
    public void createTestIdempotent() throws Exception {
        final String employeeJSON = jsonTester.write(employee).getJson();
        EmployeeWrapper employeeWrapper = EmployeeWrapper.builder()
                .employee(employee)
                .isIdempotent(true)
                .build();

        when(service.saveEmployee(Mockito.any())).thenReturn(employeeWrapper);
        mvc.perform(post("/employees")
                .content(employeeJSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    public void findEmployeeByIdTest() throws Exception {

        when(service.retrieveEmployeeById(anyString())).thenReturn(employee);

        mvc.perform(get("/employees/"+employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void findEmployeeByIdNegativeTest() throws Exception {

        when(service.retrieveEmployeeById(anyString())).thenThrow(ItemNotFoundException.class);

        mvc.perform(get("/employees/"+employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void findEmployeeByFirstNameTest() throws Exception {
        when(service.retrieveEmployeeByFirstName(anyString())).thenReturn(employee);

        mvc.perform(get("/employees/names/"+employee.getFirstName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void findEmployeeByFirstNameNegativeTest() throws Exception {
        when(service.retrieveEmployeeByFirstName(anyString())).thenThrow(ItemNotFoundException.class);

        mvc.perform(get("/employees/names/"+employee.getFirstName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void findEmployeeByDepartmentNameTest() throws Exception {
        when(service.retrieveEmployeesByDepartmentName(anyString())).thenReturn(Collections.singletonList(employee));

        mvc.perform(get("/employees/departments/names/"+employee.getDepartment().getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    @Test
    public void findEmployeeByDepartmentNameNegativeTest() throws Exception {
        when(service.retrieveEmployeesByDepartmentName(anyString())).thenThrow(ItemNotFoundException.class);

        mvc.perform(get("/employees/departments/names"+employee.getDepartment().getName())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateEmployeeTest() throws Exception {
        Employee.EmployeeBuilder employeeBuilder = employee.toBuilder();
        Employee updateDetails = employeeBuilder.firstName("Vish").lastName("Rane").id(employee.getId()).build();

        final String updateDetailsJson = jsonTester.write(updateDetails).getJson();
        when(service.updateEmployee(any())).thenReturn(updateDetails);

        mvc.perform(put("/employees/"+employee.getId())
                .content(updateDetailsJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Vish"));
    }


    @Test
    public void updateEmployeeNegativeTest() throws Exception {
        //NULL employeeId in URL
        Employee.EmployeeBuilder employeeBuilder = employee.toBuilder();
        Employee updateDetails = employeeBuilder.firstName("Vish").lastName("Rane").id(null).build();
        final String updateDetailsJson = jsonTester.write(updateDetails).getJson();
        when(service.updateEmployee(any())).thenReturn(updateDetails);

        mvc.perform(put("/employees/"+employee.getId())
                .content(updateDetailsJson)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());


        //Mismatched employeeIds in URL and request payload
        Employee.EmployeeBuilder employeeBuilder2 = employee.toBuilder();
        Employee updateDetails2 = employeeBuilder2.firstName("Vish").lastName("Rane").id("123").build();
        final String updateDetailsJson2 = jsonTester.write(updateDetails2).getJson();
        when(service.updateEmployee(any())).thenReturn(updateDetails2);

        mvc.perform(put("/employees/"+employee.getId())
                .content(updateDetailsJson2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }



    @Test
    public void deleteEmployeeTest() throws Exception {
        mvc.perform(delete("/employees/"+employee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    public void deleteEmployeeNegativeTest() throws Exception {
        doThrow(ItemNotFoundException.class).when(service).deleteEmployee(anyString());
        mvc.perform(delete("/employees/"+null)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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
