package com.beyond.university.department.model.vo;

import com.beyond.university.department.model.dto.DepartmentRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department {
    private String no;

    private String name;

    private String category;

    private String openYn;

    private int capacity;

    public void setDepartment(DepartmentRequestDto requestDto) {
        this.name = requestDto.getName();
        this.category = requestDto.getCategory();
        this.openYn = requestDto.getOpenYn();
        this.capacity = requestDto.getCapacity();
    }
}
