package com.xdw.demobackend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页响应格式
 * @param <T> 数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {
    
    /**
     * 数据列表
     */
    private List<T> data;
    
    /**
     * 分页信息（直接使用PageHelper的PageInfo）
     */
    private PageInfo<T> pagination;
    
    /**
     * 排序信息
     */
    private SortInfo sort;
    
    /**
     * 筛选信息
     */
    private FilterInfo filter;
    
    /**
     * 额外的统计信息
     */
    private Object summary;
    
    // ============ 静态工厂方法 ============
    
    /**
     * 从PageHelper的PageInfo创建分页响应
     */
    public static <T> PageResponse<T> fromPageInfo(PageInfo<T> pageInfo) {
        return PageResponse.<T>builder()
                .data(pageInfo.getList())
                .pagination(pageInfo)
                .build();
    }
    
    /**
     * 从PageHelper的PageInfo创建分页响应（转换数据类型）
     */
    public static <T, R> PageResponse<R> fromPageInfo(PageInfo<T> pageInfo, List<R> convertedData) {
        // 创建一个新的PageInfo，但使用转换后的数据
        PageInfo<R> newPageInfo = new PageInfo<>();
        newPageInfo.setPageNum(pageInfo.getPageNum());
        newPageInfo.setPageSize(pageInfo.getPageSize());
        newPageInfo.setSize(convertedData.size());
        newPageInfo.setTotal(pageInfo.getTotal());
        newPageInfo.setPages(pageInfo.getPages());
        newPageInfo.setList(convertedData);
        newPageInfo.setPrePage(pageInfo.getPrePage());
        newPageInfo.setNextPage(pageInfo.getNextPage());
        newPageInfo.setIsFirstPage(pageInfo.isIsFirstPage());
        newPageInfo.setIsLastPage(pageInfo.isIsLastPage());
        newPageInfo.setHasPreviousPage(pageInfo.isHasPreviousPage());
        newPageInfo.setHasNextPage(pageInfo.isHasNextPage());
        newPageInfo.setNavigatePages(pageInfo.getNavigatePages());
        newPageInfo.setNavigatepageNums(pageInfo.getNavigatepageNums());
        newPageInfo.setNavigateFirstPage(pageInfo.getNavigateFirstPage());
        newPageInfo.setNavigateLastPage(pageInfo.getNavigateLastPage());
        
        return PageResponse.<R>builder()
                .data(convertedData)
                .pagination(newPageInfo)
                .build();
    }
    
    /**
     * 创建空的分页响应
     */
    public static <T> PageResponse<T> empty() {
        PageInfo<T> emptyPageInfo = new PageInfo<>();
        emptyPageInfo.setPageNum(1);
        emptyPageInfo.setPageSize(10);
        emptyPageInfo.setTotal(0);
        emptyPageInfo.setPages(0);
        emptyPageInfo.setList(List.of());
        
        return PageResponse.<T>builder()
                .data(List.of())
                .pagination(emptyPageInfo)
                .build();
    }
    
    // ============ 链式调用方法 ============
    
    /**
     * 添加统计信息
     */
    public PageResponse<T> withSummary(Object summary) {
        this.summary = summary;
        return this;
    }
    
    /**
     * 添加筛选信息
     */
    public PageResponse<T> withFilter(FilterInfo filter) {
        this.filter = filter;
        return this;
    }
    
    /**
     * 添加排序信息
     */
    public PageResponse<T> withSort(SortInfo sort) {
        this.sort = sort;
        return this;
    }
}