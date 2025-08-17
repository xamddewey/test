package com.xdw.demobackend.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleType {
    SUPER_ADMIN("SUPER_ADMIN", "超级管理员"),
    ADMIN("ADMIN", "管理员"),
    LEDGER_OWNER("LEDGER_OWNER", "账本所有者"),
    LEDGER_PARTICIPANT("LEDGER_PARTICIPANT", "账本参与者");

    private final Integer id = ordinal() + 1; // 从1开始的ID
    private final String name;
    private final String description;
}
