package com.xdw.demobackend.service.expense.impl;

import com.xdw.demobackend.dto.expense.CategoryRequest;
import com.xdw.demobackend.dto.expense.CategoryResponse;
import com.xdw.demobackend.entity.*;
import com.xdw.demobackend.repository.*;
import com.xdw.demobackend.service.expense.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final ExpenseCategoryRepository expenseCategoryRepository;
    private final LedgerCategoryRepository ledgerCategoryRepository;
    private final AccountLedgerRepository accountLedgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    
    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request, Long userId) {
        if (expenseCategoryRepository.existsByCategoryNameAndIsDeletedFalse(request.getCategoryName())) {
            throw new RuntimeException("类别名称已存在: " + request.getCategoryName());
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        ExpenseCategory category = expenseCategoryRepository.insert(
                ExpenseCategoryDraft.$.produce(draft -> {
                    draft.setCategoryName(request.getCategoryName());
                    draft.setDescription(request.getDescription());
                    draft.setIsDefault(false);
                    draft.setIsSystem(false);
                    draft.setCreator(UserDraft.$.produce(u -> u.setId(userId)));
                    draft.setDisplayOrder(999);
                    draft.setUsageCount(0);
                    draft.setCreatedAt(now);
                    draft.setUpdatedAt(now);
                    draft.setIsDeleted(false);
                })
        );
        
        log.info("Category created successfully, ID: {}", category.id());
        return CategoryResponse.fromEntity(category);
    }
    
    @Override
    public List<CategoryResponse> getSystemCategories() {
        List<ExpenseCategory> categories = expenseCategoryRepository
                .findByIsSystemTrueAndIsDeletedFalse();
        
        return categories.stream()
                .map(CategoryResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryResponse> getLedgerCategories(Long ledgerId, Long userId) {
        if (!accountLedgerRepository.existsByIdAndIsDeletedFalse(ledgerId)) {
            throw new RuntimeException("账本不存在，ID: " + ledgerId);
        }
        
        if (!ledgerMemberRepository.existsByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)) {
            throw new RuntimeException("无权访问该账本");
        }
        
        List<LedgerCategory> ledgerCategories = ledgerCategoryRepository
                .findByLedgerIdAndIsDeletedFalseOrderByDisplayOrderAsc(ledgerId);
        
        return ledgerCategories.stream()
                .map(lc -> {
                    ExpenseCategory category = expenseCategoryRepository.findByIdAndIsDeletedFalse(lc.categoryId())
                            .orElse(null);
                    return category != null ? CategoryResponse.fromEntity(category) : null;
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void addCategoryToLedger(Long ledgerId, Long categoryId, Long userId) {
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(ledgerId)
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + ledgerId));
        
        LedgerMember member = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)
                .orElseThrow(() -> new RuntimeException("无权操作该账本"));
        
        if (member.joinStatus() != LedgerMember.JoinStatus.JOINED) {
            throw new RuntimeException("只有已加入的成员才能添加类别");
        }
        
        ExpenseCategory category = expenseCategoryRepository.findByIdAndIsDeletedFalse(categoryId)
                .orElseThrow(() -> new RuntimeException("类别不存在，ID: " + categoryId));
        
        if (ledgerCategoryRepository.existsByLedgerIdAndCategoryIdAndIsDeletedFalse(ledgerId, categoryId)) {
            throw new RuntimeException("该类别已添加到账本中");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        long maxDisplayOrder = ledgerCategoryRepository.findByLedgerIdAndIsDeletedFalse(ledgerId)
                .stream()
                .mapToInt(lc -> lc.displayOrder() != null ? lc.displayOrder() : 0)
                .max()
                .orElse(0);
        
        ledgerCategoryRepository.insert(
                LedgerCategoryDraft.$.produce(draft -> {
                    draft.setLedger(AccountLedgerDraft.$.produce(l -> l.setId(ledgerId)));
                    draft.setCategory(ExpenseCategoryDraft.$.produce(c -> c.setId(categoryId)));
                    draft.setDisplayOrder((int) maxDisplayOrder + 1);
                    draft.setCategoryName(category.categoryName());
                    draft.setUsageCount(0);
                    draft.setCreatedAt(now);
                    draft.setUpdatedAt(now);
                    draft.setIsDeleted(false);
                })
        );
        
        log.info("Category {} added to ledger {}", categoryId, ledgerId);
    }
    
    @Override
    @Transactional
    public void removeCategoryFromLedger(Long ledgerId, Long categoryId, Long userId) {
        AccountLedger ledger = accountLedgerRepository.findByIdAndIsDeletedFalse(ledgerId)
                .orElseThrow(() -> new RuntimeException("账本不存在，ID: " + ledgerId));
        
        LedgerMember member = ledgerMemberRepository
                .findByLedgerIdAndUserIdAndIsDeletedFalse(ledgerId, userId)
                .orElseThrow(() -> new RuntimeException("无权操作该账本"));
        
        if (member.joinStatus() != LedgerMember.JoinStatus.JOINED) {
            throw new RuntimeException("只有已加入的成员才能移除类别");
        }
        
        LedgerCategory ledgerCategory = ledgerCategoryRepository
                .findByLedgerIdAndCategoryIdAndIsDeletedFalse(ledgerId, categoryId)
                .orElseThrow(() -> new RuntimeException("该类别未添加到账本中"));
        
        if (ledgerCategory.usageCount() != null && ledgerCategory.usageCount() > 0) {
            throw new RuntimeException("该类别已被使用，无法移除");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        LedgerCategory deleted = LedgerCategoryDraft.$.produce(ledgerCategory, draft -> {
            draft.setIsDeleted(true);
            draft.setDeletedAt(now);
        });
        
        ledgerCategoryRepository.save(deleted);
        
        log.info("Category {} removed from ledger {}", categoryId, ledgerId);
    }
}
