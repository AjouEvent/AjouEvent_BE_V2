package com.example.ajouevent_be_v2.common.dto;

import java.util.List;

public record SliceResult<T>(
        List<T> result,
        boolean hasPrevious,
        boolean hasNext,
        int currentPage,
        SortInfo sort
) {
    public record SortInfo(
            boolean sorted,
            String direction,
            String orderProperty
    ) {
    }
}
