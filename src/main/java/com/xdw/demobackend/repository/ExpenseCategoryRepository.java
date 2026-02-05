package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.ExpenseCategory;
import org.babyfish.jimmer.spring.repository.JRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseCategoryRepository extends JRepository<ExpenseCategory, Long> {
    
    Optional<ExpenseCategory> findByIdAndIsDeletedFalse(Long id);
    
    Optional<ExpenseCategory> findByCategoryNameAndIsDeletedFalse(String categoryName);
    
    List<ExpenseCategory> findByIsDefaultTrueAndIsDeletedFalse();
    
    List<ExpenseCategory> findByIsSystemTrueAndIsDeletedFalse();
    
    List<ExpenseCategory> findByCreatorIdAndIsDeletedFalse(Long creatorId);
    
    List<ExpenseCategory> findByIsDeletedFalseOrderByDisplayOrderAsc();
    
    boolean existsByCategoryNameAndIsDeletedFalse(String categoryName);
}
