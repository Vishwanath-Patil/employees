package com.springboot.employees.controller;

import com.springboot.employees.domain.Employee;
import com.springboot.employees.domain.EmployeeWrapper;
import com.springboot.employees.exceptions.BadRequestException;
import com.springboot.employees.exceptions.ItemNotFoundException;
import com.springboot.employees.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.text.ParseException;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/employees")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    @Operation(summary = "This method creates a Employee resource with requisite details and adds it to the database.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "The Employee resource has been created successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))}),
            @ApiResponse(responseCode = "400", description = "Employee data is invalid, e.g. primary emailAddress is missing.")})
    @PostMapping
    public EntityModel<Employee> create(@RequestBody @Valid Employee employee, HttpServletResponse response) {

        log.info("Inside create method of the Employee controller.");

        EmployeeWrapper employeeWrapper = service.saveEmployee(employee);
        EntityModel<Employee> resource = EntityModel.of(employeeWrapper.getEmployee());

        resource.add(getEmployeeSelfLink(employeeWrapper.getEmployee().getId()));
        response.setHeader("Location", String.valueOf(linkTo(EmployeeController.class).slash(employeeWrapper.getEmployee().getId()).toUri()));

        if (!employeeWrapper.isIdempotent()) {
            response.setStatus(HttpServletResponse.SC_CREATED);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }

        return resource;
    }

    @Operation(description = "This method fetches a employee resource from the database based on the given employee id.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The employee resource requested has been fetched successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))}),
            @ApiResponse(responseCode = "404", description = "Employee requested for the given id doesn't exist in the database.")})
    @GetMapping("/{id}")
    public EntityModel<Employee> findEmployeeById(@Parameter(description = "The id of the employee to look up.") @PathVariable("id") String employeeId) {
        log.info("Inside findEmployeeById method of the Employee controller.");

        try {
            Employee employee = service.retrieveEmployeeById(employeeId);
            EntityModel<Employee> resource = EntityModel.of(employee);
            resource.add(getEmployeeSelfLink(employee.getId()));
            return resource;
        } catch (Exception e) {
            throw new ItemNotFoundException("The employee with id : "+employeeId+" could not be found!");
        }

    }


    @Operation(description = "This method fetches a employee resource from the database based on the given employee firstName.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The employee resource requested has been fetched successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))}),
            @ApiResponse(responseCode = "404", description = "Employee requested for the given firstName doesn't exist in the database.")})
    @GetMapping("/names/{firstName}")
    public EntityModel<Employee> findEmployeeByFirstName(@Parameter(description = "The first name of the employee to look up.") @PathVariable("firstName") String employeeFirstName) {
        log.info("Inside findEmployeeByFirstName method of the Employee controller.");

        try {
            Employee employee = service.retrieveEmployeeByFirstName(employeeFirstName);
            EntityModel<Employee> resource = EntityModel.of(employee);
            resource.add(linkTo(EmployeeController.class).slash("names").slash(employeeFirstName).withSelfRel());
            return resource;
        } catch (Exception e) {
            throw new ItemNotFoundException("The employee with firstName : "+employeeFirstName+" could not be found!");
        }

    }


    @Operation(description = "This method fetches a employee resource from the database based on the given employee's department name.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The employee resource requested has been fetched successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))}),
            @ApiResponse(responseCode = "404", description = "Employee requested for the given department name doesn't exist in the database.")})
    @GetMapping("/departments/names/{departmentName}")
    public CollectionModel<EntityModel<Employee>> findEmployeeByDepartmentName(@Parameter(description = "The department name of the employee to look up.") @PathVariable("departmentName") String departmentName) {
        log.info("Inside findEmployeeByDepartmentName method of the Employee controller.");

        try {
            List<Employee> employees = service.retrieveEmployeesByDepartmentName(departmentName);
            List<EntityModel<Employee>> employeeEntityList =  employees.stream()
                    .map(emp -> {
                            EntityModel<Employee> resource = EntityModel.of(emp);
                            resource.add(getEmployeeSelfLink(emp.getId()));
                            return resource;
                    })
                    .collect(Collectors.toList());
            CollectionModel<EntityModel<Employee>> resource = CollectionModel.of(employeeEntityList);
            resource.add(linkTo(EmployeeController.class).slash("departments").slash("names").slash(departmentName).withSelfRel());
            return resource;
        } catch (Exception e) {
            throw new ItemNotFoundException("The employee with firstName : "+departmentName+" could not be found!");
        }
    }


    @Operation(summary = "This method updates a Employee resource with requisite details and updates it to the database.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "The Employee resource has been updated successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))}),
            @ApiResponse(responseCode = "400", description = "Employee data is invalid, e.g. primary emailAddress is missing."),
            @ApiResponse(responseCode = "409", description = "Employee data with same update already exists.")})
    @PutMapping("/{id}")
    public EntityModel<Employee> updateEmployee(@Parameter(description = "The id of the employee to update.") @PathVariable String id, @RequestBody @Valid Employee employeeUpdate, HttpServletResponse response) throws ParseException {

        log.info("Inside updateEmployee method of the Employee controller.");

        if(null == employeeUpdate.getId()) {
            throw new BadRequestException("Update payload should include the employee id!");
        }

        if(!id.equals(employeeUpdate.getId())) {
            throw new BadRequestException("Employee id in payload and url must match!");
        }

        Employee employee = service.updateEmployee(employeeUpdate);
        EntityModel<Employee> resource = EntityModel.of(employee);

        resource.add(getEmployeeSelfLink(employee.getId()));
        response.setStatus(HttpServletResponse.SC_OK);

        return resource;
    }


    @Operation(summary = "This method deletes a Employee resource from the database for the given employee id.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "The Employee resource has been deleted successfully.",
                    content = { @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Employee.class))}),
            @ApiResponse(responseCode = "404", description = "Employee data is invalid, e.g. primary emailAddress is missing.")})
    @DeleteMapping("/{id}")
    public void deleteEmployee(@Parameter(description = "The id of the employee to delete.") @PathVariable String id, HttpServletResponse response) {
        log.info("Inside deleteEmployee method of the Employee controller.");

        service.deleteEmployee(id);
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }


    private Link getEmployeeSelfLink(String id){
        return linkTo(EmployeeController.class).slash(id).withSelfRel();
    }


}
