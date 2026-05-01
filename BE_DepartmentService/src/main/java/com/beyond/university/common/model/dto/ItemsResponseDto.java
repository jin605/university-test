package com.beyond.university.common.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@ToString
public class ItemsResponseDto<T> extends BaseResponseDto<T> {
    @Schema(description = "페이지 번호", example = "1")
    private final int page;

    @Schema(description = "조회 결과 수", example = "10")
    private final int numOfRows;

    @Schema(description = "전체 결과 수", example = "100")
    private final int totalCount;

    public ItemsResponseDto(HttpStatus httpStatus, List<T> items, int page, int totalCount) {
        super(httpStatus, items);
        this.page = page;
        this.numOfRows = items.size();
        this.totalCount = totalCount;
    }
}
