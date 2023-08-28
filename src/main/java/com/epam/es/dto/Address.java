package com.epam.es.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Address {
    @JsonSerialize
    @JsonDeserialize
    private String country;
    @JsonSerialize
    @JsonDeserialize
    private String town;
}
