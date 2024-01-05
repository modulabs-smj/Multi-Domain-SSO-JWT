package com.bandall.location_share.web.controller.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PageDto<T> {

    List<T> elements;

    int curPage;

    int totalPage;

    long totalElements;

    public PageDto(Page<T> page) {
        this.elements = page.getContent();
        this.curPage = page.getNumber();
        this.totalPage = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }
}
