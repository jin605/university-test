package com.beyond.university.department.controller;

import com.beyond.university.common.exception.UniversityException;
import com.beyond.university.common.exception.message.ExceptionMessage;
import com.beyond.university.common.model.dto.BaseResponseDto;
import com.beyond.university.common.model.dto.ItemsResponseDto;
import com.beyond.university.department.model.dto.DepartmentRequestDto;
import com.beyond.university.department.model.service.DepartmentService;
import com.beyond.university.department.model.vo.Department;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/*
    학과 관련 API

    1. 학과 목록 조회
      - GET /api/v1/department-service/departments

    2. 학과 상세 조회
      - GET /api/v1/department-service/departments/{department-no}

    3. 학과 등록
      - POST /api/v1/department-service/departments

    4. 학과 수정
      - PUT /api/v1/department-service/departments/{department-no}

    5. 학과 삭제
      - DELETE /api/v1/department-service/departments/{department-no}
 */

@Slf4j
@RestController
@RequestMapping("/api/v1/department-service")
@RequiredArgsConstructor
@Tag(name = "Departments APIs", description = "학과 관련 API 목록")
public class DepartmentController {
    private final DepartmentService departmentService;

    @GetMapping("/departments")
    @Operation(summary = "학과 목록 조회", description = "학과의 목록을 조회한다.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호", example = "1"),
            @Parameter(name = "numOfRows", description = "한 페이지 결과 수", example = "10"),
            @Parameter(name = "openYn", description = "개설 여부", example = "Y")
    })
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
                    // content = @Content(
                    //         mediaType = MediaType.APPLICATION_JSON_VALUE,
                    //         schema = @Schema(implementation = DepartmentsResponseDto.class)
                    // )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "UNAUTHORIZED",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "DEPARTMENT_NOT_FOUND",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<ItemsResponseDto<Department>> getDepartments(@RequestParam int page,
                                                           @RequestParam int numOfRows,
                                                           @RequestParam(required = false) String openYn) {
        int totalCount = departmentService.getDepartmentsCount(openYn);
        List<Department> departments =
                departmentService.getDepartments(page, numOfRows, openYn);

        if (departments.isEmpty()) {
            throw new UniversityException(ExceptionMessage.DEPARTMENT_NOT_FOUND);
        }

        return ResponseEntity.ok(
                new ItemsResponseDto<>(HttpStatus.OK, departments, page, totalCount)
        );
    }

    @GetMapping({"/departments/{department-no}", "/department/{department-no}"})
    @Operation(summary = "학과 상세 조회", description = "학과 번호로 학과의 상세 정보를 조회한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "UNAUTHORIZED",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "DEPARTMENT_NOT_FOUND",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    public ResponseEntity<BaseResponseDto<Department>> getDepartment(
            @Parameter(description = "학과 번호", example = "001") @PathVariable("department-no") String departmentNo) {

        Department department = departmentService.getDepartmentByNo(departmentNo)
                .orElseThrow(() -> new UniversityException(ExceptionMessage.DEPARTMENT_NOT_FOUND));

        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, department));
    }

    @PostMapping("/departments")
    @Operation(summary = "학과 등록", description = "학과 정보를 JSON 문자열로 받아서 등록한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "CREATED",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "UNAUTHORIZED",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    // public ResponseEntity<Void> create(@RequestBody DepartmentRequestDto requestDto) {
    public ResponseEntity<BaseResponseDto<Department>> create(
            @Valid @RequestBody DepartmentRequestDto requestDto) {
        Department department = requestDto.toDepartment();

        departmentService.save(department);

        log.info("Department No : {}", department.getNo());

        // return ResponseEntity.created(URI.create("/api/v1/department-service/departments/" + department.getNo())).build();
        return ResponseEntity
                .created(URI.create("/api/v1/department-service/departments/" + department.getNo()))
                .body(new BaseResponseDto<>(HttpStatus.CREATED, department));
    }

    @PutMapping({"/departments/{department-no}", "/department/{department-no}"})
    @Operation(summary = "학과 정보 수정", description = "학과 정보를 JSON 문자열로 받아서 수정한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "BAD_REQUEST",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "UNAUTHORIZED",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "DEPARTMENT_NOT_FOUND",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    // public ResponseEntity<Void> update(
    public ResponseEntity<BaseResponseDto<Department>> update(
            @Parameter(description = "학과 번호", example = "064") @PathVariable("department-no") String departmentNo,
            @Valid @RequestBody DepartmentRequestDto requestDto) {

        Department department = departmentService.getDepartmentByNo(departmentNo)
                .orElseThrow(() -> new UniversityException(ExceptionMessage.DEPARTMENT_NOT_FOUND));

        department.setDepartment(requestDto);

        departmentService.save(department);

        // return ResponseEntity.noContent().build();
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, department));
    }


    @DeleteMapping({"/departments/{department-no}", "/department/{department-no}"})
    @Operation(summary = "학과 삭제", description = "학과 번호로 해당 학과를 삭제한다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "OK",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "UNAUTHORIZED",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "FORBIDDEN",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "DEPARTMENT_NOT_FOUND",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "INTERNAL_SERVER_ERROR",
                    content =  @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
    })
    // public ResponseEntity<Void> delete(
    public ResponseEntity<BaseResponseDto<Department>> delete(
            @Parameter(description = "학과 번호", example = "064") @PathVariable("department-no") String departmentNo) {

        Department department = departmentService.getDepartmentByNo(departmentNo)
                .orElseThrow(() -> new UniversityException(ExceptionMessage.DEPARTMENT_NOT_FOUND));

        departmentService.delete(department.getNo());

        // return ResponseEntity.noContent().build();
        return ResponseEntity.ok(new BaseResponseDto<>(HttpStatus.OK, department));
    }
}
