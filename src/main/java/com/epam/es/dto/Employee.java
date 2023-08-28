package com.epam.es.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import lombok.*;
import springfox.documentation.spring.web.plugins.JacksonSerializerConvention;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class Employee {
    @JsonSerialize
    @JsonDeserialize
    private String name;

    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate dob;
    @JsonSerialize
    @JsonDeserialize
    private Address address;
    @JsonSerialize
    @JsonDeserialize
    private String email;
    @JsonSerialize
    @JsonDeserialize
    private List<String> skills;
    @JsonSerialize
    @JsonDeserialize
    private double experience;
    @JsonSerialize
    @JsonDeserialize
    private double rating;
    @JsonSerialize
    @JsonDeserialize
    private String description;
    @JsonSerialize
    @JsonDeserialize
    private boolean verified;
    @JsonSerialize
    @JsonDeserialize
    private int salary;
}
