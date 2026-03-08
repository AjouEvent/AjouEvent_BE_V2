package com.example.ajouevent_be_v2.common.dto;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SliceResponse<T> implements Serializable {

    protected List<T> result;
    protected boolean hasPrevious;
    protected boolean hasNext;
    protected Integer currentPage;
    protected SortResponse sort;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SortResponse {
        protected boolean sorted;
        protected String direction;
        protected String orderProperty;
    }
}
