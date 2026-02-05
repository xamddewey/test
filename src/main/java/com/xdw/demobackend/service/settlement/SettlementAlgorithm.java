package com.xdw.demobackend.service.settlement;

import com.xdw.demobackend.dto.settlement.TransferDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 结算算法 - 计算最少转账次数
 */
public class SettlementAlgorithm {

    private SettlementAlgorithm() {
    }

    /**
     * 计算最少转账次数的结算列表
     *
     * @param balances 用户余额映射 (正数=应收, 负数=应付)
     * @return 转账列表
     */
    public static List<TransferDTO> calculateMinimumTransfers(Map<Long, BigDecimal> balances) {
        List<Balance> creditors = new ArrayList<>();
        List<Balance> debtors = new ArrayList<>();

        for (Map.Entry<Long, BigDecimal> entry : balances.entrySet()) {
            BigDecimal amount = entry.getValue();
            if (amount == null) {
                continue;
            }
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(new Balance(entry.getKey(), amount));
            } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(new Balance(entry.getKey(), amount.abs()));
            }
        }

        creditors.sort(Comparator.comparing(Balance::amount).reversed());
        debtors.sort(Comparator.comparing(Balance::amount).reversed());

        List<TransferDTO> transfers = new ArrayList<>();
        int i = 0;
        int j = 0;

        while (i < creditors.size() && j < debtors.size()) {
            Balance creditor = creditors.get(i);
            Balance debtor = debtors.get(j);

            BigDecimal transferAmount = creditor.amount().min(debtor.amount());
            if (transferAmount.compareTo(BigDecimal.ZERO) > 0) {
                transfers.add(new TransferDTO(
                        debtor.userId(),
                        null,
                        creditor.userId(),
                        null,
                        transferAmount
                ));
            }

            creditor.setAmount(creditor.amount().subtract(transferAmount));
            debtor.setAmount(debtor.amount().subtract(transferAmount));

            if (creditor.amount().compareTo(BigDecimal.ZERO) == 0) {
                i++;
            }
            if (debtor.amount().compareTo(BigDecimal.ZERO) == 0) {
                j++;
            }
        }

        return transfers;
    }

    private static class Balance {
        private final Long userId;
        private BigDecimal amount;

        private Balance(Long userId, BigDecimal amount) {
            this.userId = userId;
            this.amount = amount;
        }

        public Long userId() {
            return userId;
        }

        public BigDecimal amount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }
    }
}
