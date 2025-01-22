package com.artisoft.fitbuddy.dto;

import lombok.Data;

@Data
public class ExerciseSearchCriteria {
    private String name;
    private Boolean standaloneOnly = false;
}
