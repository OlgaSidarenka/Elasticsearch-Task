package com.epam.es.controller;

import com.epam.es.dto.Employee;
import com.epam.es.service.EmployeeService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<Employee> findAll() throws IOException {
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable String id) throws IOException {
        return employeeService.getEmployeeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable String id) throws IOException {
        return employeeService.deleteEmployee(id);
    }

    @PostMapping("/{id}")
    public ResponseEntity<String> createEmployee(@RequestBody Employee employee, @PathVariable String id) throws IOException {
        return ResponseEntity.ok(employeeService.createEmployee(employee, id));
    }

    @GetMapping("/search")
    public List<Employee> searchByField(@RequestParam(value = "fieldName") String fieldName, @RequestParam(value = "fieldValue") String fieldValue) throws IOException {
        return employeeService.searchByField(fieldName, fieldValue);
    }

    @GetMapping("/aggregate")
    public ResponseEntity<JsonNode> aggregateEmployees(@RequestParam(value = "aggrField") String aggrField,
                                                       @RequestParam(value = "metricType") String metricType,
                                                       @RequestParam(value = "metricField") String metricField) throws IOException {
        return ResponseEntity.ok(employeeService.aggregateEmployees(aggrField, metricType, metricField));
    }

}
