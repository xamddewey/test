package com.xdw.demobackend.repository;

import com.xdw.demobackend.entity.Role;
import com.xdw.demobackend.entity.RoleDraft;
import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.entity.UserDraft;
import com.xdw.demobackend.entity.UserRole;
import com.xdw.demobackend.entity.UserRoleDraft;
import com.xdw.demobackend.enums.RoleType;
import lombok.extern.slf4j.Slf4j;
import org.babyfish.jimmer.UnloadedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserRoleRepositoryBenchmarkTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private User testUser;
    private List<Role> testRoles;

    @BeforeEach
    void setUp() {
        testUser = userRepository.insert(
            UserDraft.$.produce(draft -> {
                draft.setUsername("benchmark_user");
                draft.setEmail("benchmark@test.com");
                draft.setPassword("test123");
                draft.setNickname("Benchmark User");
                draft.setCreatedAt(LocalDateTime.now());
                draft.setUpdatedAt(LocalDateTime.now());
            })
        );

        testRoles = createTestRoles(5);

        for (Role role : testRoles) {
            userRoleRepository.insert(
                UserRoleDraft.$.produce(draft -> {
                    draft.setUser(UserDraft.$.produce(ud -> ud.setId(testUser.id())));
                    draft.setRole(RoleDraft.$.produce(rd -> rd.setId(role.id())));
                    draft.setCreatedAt(LocalDateTime.now());
                    draft.setUpdatedAt(LocalDateTime.now());
                })
            );
        }
    }

    @Test
    void benchmarkLazyLoadingVsEagerLoading() {
        log.info("Starting benchmark test with {} roles per user", testRoles.size());

        benchmarkLazyLoading();
        benchmarkEagerLoading();

        log.info("Benchmark test completed");
    }

    private void benchmarkLazyLoading() {
        log.info("\n" + "=".repeat(70));
        log.info("LAZY LOADING BENCHMARK (findByUserId)");
        log.info("=".repeat(70));

        long startTime = System.nanoTime();

        List<UserRole> userRoles = userRoleRepository.findByUserId(testUser.id());

        List<String> roleNames = new ArrayList<>();
        int unloadedExceptions = 0;

        for (UserRole userRole : userRoles) {
            try {
                roleNames.add(userRole.role().roleName());
            } catch (UnloadedException e) {
                unloadedExceptions++;
                log.warn("UnloadedException caught: {}", e.getMessage());
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;

        log.info("Operation: Load {} user roles and access each role name", testRoles.size());
        log.info("Duration: {} ms", duration);
        log.info("Query Pattern: 1 (user_roles) + {} (lazy-loaded roles) = {} queries expected", testRoles.size(), testRoles.size() + 1);
        log.info("UnloadedException Errors: {}", unloadedExceptions);
        log.info("Successfully loaded role names: {}", roleNames);
        log.info("=".repeat(70));
    }

    private void benchmarkEagerLoading() {
        log.info("\n" + "=".repeat(70));
        log.info("EAGER LOADING BENCHMARK (findByUserIdWithRoles - Fetcher API)");
        log.info("=".repeat(70));

        long startTime = System.nanoTime();

        List<UserRole> userRoles = userRoleRepository.findByUserIdWithRoles(testUser.id());

        List<String> roleNames = new ArrayList<>();

        for (UserRole userRole : userRoles) {
            roleNames.add(userRole.role().roleName());
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000;

        log.info("Operation: Load {} user roles and access each role name", testRoles.size());
        log.info("Duration: {} ms", duration);
        log.info("Query Pattern: 1 (LEFT JOIN with roles)", testRoles.size());
        log.info("UnloadedException Errors: 0");
        log.info("Successfully loaded role names: {}", roleNames);
        log.info("=".repeat(70));
    }

    private List<Role> createTestRoles(int count) {
        List<Role> roles = new ArrayList<>();
        String roleType = RoleType.LEDGER_PARTICIPANT.getName();

        for (int i = 0; i < count; i++) {
            final int index = i;
            Role role = roleRepository.insert(
                RoleDraft.$.produce(draft -> {
                    draft.setRoleName("BENCH_ROLE_" + index);
                    draft.setDescription("Benchmark test role " + index);
                    draft.setRoleType(roleType);
                    draft.setCreatedAt(LocalDateTime.now());
                    draft.setUpdatedAt(LocalDateTime.now());
                })
            );
            roles.add(role);
        }

        return roles;
    }
}
