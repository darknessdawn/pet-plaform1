package com.pet.controller;

import com.pet.common.Result;
import com.pet.service.MembershipService;
import com.pet.service.PointsService;
import com.pet.vo.MembershipLevelVO;
import com.pet.vo.PointsProductVO;
import com.pet.vo.UserMembershipVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/membership")
@RequiredArgsConstructor
@Tag(name = "会员管理", description = "会员等级、积分等接口")
public class MembershipController {

    private final MembershipService membershipService;

    @Operation(summary = "获取会员等级列表")
    @GetMapping("/levels")
    public Result<List<MembershipLevelVO>> getMembershipLevels() {
        List<MembershipLevelVO> list = membershipService.getMembershipLevels();
        return Result.success(list);
    }

    @Operation(summary = "我的会员信息")
    @GetMapping("/my")
    public Result<UserMembershipVO> getMyMembership(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        UserMembershipVO vo = membershipService.getMyMembership(userId);
        return Result.success(vo);
    }

    @Operation(summary = "添加成长值")
    @PostMapping("/growth/add")
    public Result<Boolean> addGrowthValue(
            @RequestParam Integer value,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        boolean result = membershipService.addGrowthValue(userId, value);
        return Result.success(result);
    }
}
