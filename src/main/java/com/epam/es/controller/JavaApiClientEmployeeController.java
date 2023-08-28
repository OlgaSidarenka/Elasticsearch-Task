package com.epam.es.controller;

import com.epam.es.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/employees")
public class JavaApiClientEmployeeController extends EmployeeController {

    @Autowired
    public JavaApiClientEmployeeController(@Qualifier("javaApiClient") EmployeeService employeeService) {
        super(employeeService);
    }


}
