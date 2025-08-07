package com.example.bankcards.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

/**
 * Обертка для пагинированных данных.
 */
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class DataDto<T> {
    /** Список элементов */
    private List<T> data;
    /** Всего страниц */
    private int totalPages;
    /** Всего элементов */
    private long totalElements;
}
