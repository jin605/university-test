package com.beyond.university.department.model.dto;

import com.beyond.university.department.model.vo.Department;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class DepartmentRequestDto {
    @Schema(description = "학과 이름", example = "일어학과")
    @NotBlank
    private final String name;

    @Schema(description = "계열", example = "인문사회")
    private final String category;

    @Schema(description = "개설 여부", example = "Y")
    @Pattern(regexp = "Y|N", message = "'Y' 또는 'N'이어야 한다.")
    private final String openYn;

    @Schema(description = "정원", example = "50")
    @PositiveOrZero
    private final int capacity;

    public Department toDepartment() {

        return Department.builder()
                .name(name)
                .category(category)
                .openYn(openYn)
                .capacity(capacity)
                .build();
    }
}
