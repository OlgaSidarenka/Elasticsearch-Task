package com.epam.es.service;

import com.epam.es.dto.Employee;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface EmployeeService {
 @Autowired

    List<Employee> findAll() throws IOException;

    Optional<Employee> getEmployeeById(String id) throws IOException;

    String deleteEmployee(String id) throws IOException;

    String createEmployee(Employee employee, String id) throws IOException;

    List<Employee> searchByField(String fieldName, String fieldValue) throws IOException;

    JsonNode aggregateEmployees(String aggrField, String metricType, String metricField) throws IOException;

}
