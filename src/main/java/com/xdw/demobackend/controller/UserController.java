package com.xdw.demobackend.controller;

import com.xdw.demobackend.dto.common.ApiResult;
import com.xdw.demobackend.entity.User;
import com.xdw.demobackend.enums.ResponseCode;
import com.xdw.demobackend.security.UserPrincipal;
import com.xdw.demobackend.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 获取当前用户信息
     * 需要用户具有 SUPER_ADMIN、ADMIN、LEDGER_OWNER 或 LEDGER_PARTICIPANT 角色
     *
     * @param userPrincipal 当前用户的 UserPrincipal 对象
     * @return ApiResult 包含当前用户信息
     */
    @GetMapping("/me")
    @PreAuthorize(
        "hasRole('SUPER_ADMIN') " +
        "or hasRole('ADMIN') " +
        "or hasRole('LEDGER_OWNER') " +
        "or hasRole('LEDGER_PARTICIPANT')"
    )
    public ApiResult<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        var currentUser = userService.getCurrentUser(userPrincipal);
        return ApiResult.success("获取当前用户信息成功", currentUser);
    }

    /**
     * 根据用户ID获取用户信息
     * 需要用户具有 SUPER_ADMIN 或 ADMIN 角色
     *
     * @return ApiResult 包含用户信息
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ApiResult<?> getAllUsers() {
        var users = userService.getAllUsers();
        return ApiResult.success("获取所有用户信息成功", users);
    }

    /**
     * 根据用户ID获取用户信息
     * 需要用户具有 SUPER_ADMIN 或 ADMIN 角色
     *
     * @param id 用户ID
     * @return ApiResult 包含用户信息
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ApiResult<?> getUserById(@PathVariable Long id) {
        var user = userService.getUserById(id);
        if (user != null) {
            return ApiResult.success("获取用户信息成功", user);
        } else {
            return ApiResult.error(ResponseCode.USER_NOT_FOUND);
        }
    }

    /**
     * 根据用户名获取用户信息
     * 需要用户具有 SUPER_ADMIN 或 ADMIN 角色,
     * 或者当前请求修改的用户 ID 必须等于当前登录用户的 ID
     *
     * @param id 用户ID
     * @param user 用户对象，包含更新的信息
     * @return ApiResult 包含用户信息
     */
    @PutMapping("/{id}")
    @PreAuthorize(
        "hasRole('SUPER_ADMIN') " +
        "or hasRole('ADMIN') " +
        "or #id == authentication.principal.id"
    )
    public ApiResult<?> updateUser(
        @PathVariable Long id,
        @RequestBody User user
    ) {
        try {
            var updateUser = userService.updateUser(id, user);
            return ApiResult.success("用户信息更新成功", updateUser);
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage());
            return ApiResult.error(ResponseCode.BAD_REQUEST, "更新用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 删除用户
     * 需要用户具有 SUPER_ADMIN 或 ADMIN 角色
     *
     * @param id 用户ID
     * @return ApiResult 包含删除结果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('ADMIN')")
    public ApiResult<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ApiResult.success("用户删除成功");
        } catch (Exception e) {
            log.error("删除用户失败: {}", e.getMessage());
            return ApiResult.error(ResponseCode.BAD_REQUEST, "删除用户失败: " + e.getMessage());
        }
    }
}
