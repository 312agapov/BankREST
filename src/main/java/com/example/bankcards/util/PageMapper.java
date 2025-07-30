package com.example.bankcards.util;

import com.example.bankcards.dto.DataDto;
import org.springframework.data.domain.Page;

public class PageMapper {
    public static <T> DataDto<T> mapToDataDto(Page<T> page) {
        DataDto<T> dto = new DataDto<>();
        dto.setData(page.getContent());
        dto.setTotalPages(page.getTotalPages());
        dto.setTotalElements(page.getTotalElements());
        return dto;
    }
}
