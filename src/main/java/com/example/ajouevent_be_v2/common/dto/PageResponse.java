package com.example.ajouevent_be_v2.common.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> result,
        int currentPage,
        int totalPages,
        long totalElements,
        boolean hasNext,
        boolean hasPrevious
) {
}
