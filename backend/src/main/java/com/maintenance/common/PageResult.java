package com.maintenance.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {

    private Long total;
    private Integer pageNum;
    private Integer pageSize;
    private List<T> records;

    public static <T> PageResult<T> of(Long total, Integer pageNum, Integer pageSize, List<T> records) {
        return new PageResult<>(total, pageNum, pageSize, records);
    }
}
