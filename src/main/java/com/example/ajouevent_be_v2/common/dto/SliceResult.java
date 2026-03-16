package com.example.ajouevent_be_v2.common.dto;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

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

    public static <T> SliceResult<T> from(Slice<T> slice, Pageable pageable) {
        Sort sort = pageable.getSort();
        return new SliceResult<>(
            slice.getContent(),
            slice.hasPrevious(),
            slice.hasNext(),
            slice.getNumber(),
            new SortInfo(
                sort.isSorted(),
                sort.stream().map(o -> o.getDirection().name()).findFirst().orElse("DESC"),
                sort.stream().map(Sort.Order::getProperty).findFirst().orElse("createdAt")
            )
        );
    }
}
