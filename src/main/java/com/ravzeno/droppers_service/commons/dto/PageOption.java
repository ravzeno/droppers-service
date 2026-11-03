package com.ravzeno.droppers_service.commons.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Data
public class PageOption {

    @Min(1)
    private Integer page = 1;

    @Min(1)
    @Max(500)
    private Integer pageSize = 50;

    @JsonIgnore private final int offset;

    public PageOption(Integer page, Integer pageSize) {
        this.page = (page != null) ? page : this.page;
        this.pageSize = (pageSize != null) ? pageSize : this.pageSize;
        this.offset = (this.page - 1) * this.pageSize;
    }

    public PageRequest toPageRequest(Sort sort) {
        PageRequest pageRequest = PageRequest.of(offset, pageSize);

        if (sort != null) {
            pageRequest = pageRequest.withSort(sort);
        }

        return pageRequest;
    }

    public PageRequest toPageRequest() {
        return toPageRequest(null);
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getOffset() {
        return offset;
    }
}
