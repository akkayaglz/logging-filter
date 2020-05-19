package com.glz.rest.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Person {
    private BigDecimal id;
    private String name;
    private String surname;
}