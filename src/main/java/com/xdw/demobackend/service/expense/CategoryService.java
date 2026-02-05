package com.xdw.demobackend.service.expense;

import com.xdw.demobackend.dto.expense.CategoryRequest;
import com.xdw.demobackend.dto.expense.CategoryResponse;

import java.util.List;

public interface CategoryService {
    
    CategoryResponse createCategory(CategoryRequest request, Long userId);
    
    List<CategoryResponse> getSystemCategories();
    
    List<CategoryResponse> getLedgerCategories(Long ledgerId, Long userId);
    
    void addCategoryToLedger(Long ledgerId, Long categoryId, Long userId);
    
    void removeCategoryFromLedger(Long ledgerId, Long categoryId, Long userId);
}
