package com.example.ajouevent_be_v2.common.dto;

import java.util.List;

public record SliceResponse<T>(
        List<T> result,
        boolean hasPrevious,
        boolean hasNext,
        int currentPage,
        SortResponse sort
) {
    public record SortResponse(
            boolean sorted,
            String direction,
            String orderProperty
    ) {
    }
}
