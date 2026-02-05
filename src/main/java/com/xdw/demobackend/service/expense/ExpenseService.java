package com.xdw.demobackend.service.expense;

import com.xdw.demobackend.dto.expense.CreateExpenseRequest;
import com.xdw.demobackend.dto.expense.ExpenseResponse;
import com.xdw.demobackend.dto.expense.UpdateExpenseRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ExpenseService {
    
    ExpenseResponse createExpense(CreateExpenseRequest request, Long userId);
    
    ExpenseResponse getExpenseById(Long expenseId, Long userId);
    
    List<ExpenseResponse> getLedgerExpenses(Long ledgerId, Long userId);
    
    Page<ExpenseResponse> getLedgerExpensesPaged(Long ledgerId, Long userId, Pageable pageable);
    
    ExpenseResponse updateExpense(Long expenseId, UpdateExpenseRequest request, Long userId);
    
    void deleteExpense(Long expenseId, Long userId);
}
