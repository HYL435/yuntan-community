package com.yuntan.indentity.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuntan.indentity.dto.UserRoleDTO;
import com.yuntan.indentity.entity.User;
import com.yuntan.indentity.query.UserPageQuery;
import com.yuntan.indentity.service.IUserService;
import com.yuntan.indentity.vo.AdminUserVO;
import com.yuntan.infra.mysql.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import yuntan.common.domain.Result;

@Slf4j
@Tag(description = "后台用户相关接口", name = "用户管理")
@RestController
@RequestMapping("api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IUserService userService;

    @Schema(description = "分页查询用户列表")
    @GetMapping("/page")
    public Result<PageDTO<AdminUserVO>> pageUser(UserPageQuery userPageQuery) {

        log.info("接收到的 role 参数: {}", userPageQuery.getRole());

        log.info("分页查询用户列表，查询参数：{}", userPageQuery);

        //userService.page(userPageQuery.toMpPage(userPageQuery.getSortBy(), userPageQuery.getIsAsc()));

        Page<User> result = userService.userPage(userPageQuery);

        return Result.ok(PageDTO.of(result, AdminUserVO.class));
    }

    @Schema(description = "批量删除用户")
    @PutMapping("/delete/{id}")
    public Result<Void> deleteUsers(@PathVariable Long id) {
        log.info("批量删除用户，用户ID列表：{}", id);

        userService.removeById(id);

        return Result.ok();
    }

    @Schema(description = "启用禁用账号")
    @PutMapping("/status")
    public Result<Void> updateUserStatus(@RequestParam Long id, Integer status) {
        log.info("启用禁用账号，用户ID：{}，状态：{}", id, status);

        userService.enableOrDisableUser(id, status);

        return Result.ok();
    }

    @Schema(description = "提升用户为管理员")
    @PutMapping("/upgrade")
    public Result<Void> upgradeAdmin(@RequestBody UserRoleDTO userRoleDTO) {
        log.info("提升用户为管理员，用户ID：{}", userRoleDTO);

        userService.upgradeAdmin(userRoleDTO);

        return Result.ok();
    }

}
