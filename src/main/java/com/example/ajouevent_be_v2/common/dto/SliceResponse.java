package com.example.ajouevent_be_v2.common.dto;

import java.util.List;
import java.util.function.Function;

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

    public static <S, T> SliceResponse<T> from(SliceResult<S> result, Function<S, T> mapper) {
        SliceResult.SortInfo sort = result.sort();
        return new SliceResponse<>(
            result.result().stream().map(mapper).toList(),
            result.hasPrevious(),
            result.hasNext(),
            result.currentPage(),
            new SortResponse(sort.sorted(), sort.direction(), sort.orderProperty())
        );
    }

    public static <T> SliceResponse<T> empty(int pageNumber) {
        return new SliceResponse<>(List.of(), false, false, pageNumber, null);
    }
}
