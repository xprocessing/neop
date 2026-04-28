package com.gongziyu.neop.controller.marketing;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.annotation.OperationLog;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.MemberPackage;
import com.gongziyu.neop.service.MemberPackageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 会员套餐接口
 */
@RestController
public class MemberPackageController {

    @Autowired
    private MemberPackageService memberPackageService;

    // ===== 前台接口 =====

    /**
     * 会员套餐列表（前台）
     */
    @GetMapping("/api/member/package/list")
    public Result<IPage<MemberPackage>> frontList(PageDTO pageDTO) {
        IPage<MemberPackage> page = memberPackageService.frontList(pageDTO);
        return Result.success(page);
    }

    /**
     * 会员充值下单
     */
    @PostMapping("/api/member/order/create")
    public Result<Map<String, Object>> createOrder(@RequestBody MemberOrderDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = memberPackageService.createOrder(userId, dto.getPackageId());
        return Result.success(result);
    }

    /**
     * 会员订单支付
     */
    @PostMapping("/api/member/order/pay")
    public Result<Map<String, Object>> payOrder(@RequestBody PayDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = memberPackageService.payOrder(userId, dto.getOrderId());
        return Result.success(result);
    }

    // ===== 后台接口 =====

    /**
     * 会员套餐列表（后台）
     */
    @GetMapping("/api/admin/member/package/list")
    @OperationLog(module = "营销管理", description = "查询会员套餐列表")
    public Result<IPage<MemberPackage>> adminList(PageDTO pageDTO,
                                                    @RequestParam(required = false) String packageName,
                                                    @RequestParam(required = false) Integer status) {
        IPage<MemberPackage> page = memberPackageService.adminList(pageDTO, packageName, status);
        return Result.success(page);
    }

    /**
     * 新增套餐
     */
    @PostMapping("/api/admin/member/package/save")
    @OperationLog(module = "营销管理", description = "新增会员套餐")
    public Result<Void> save(@Valid @RequestBody PackageDTO dto) {
        MemberPackage pkg = new MemberPackage();
        pkg.setPackageName(dto.getPackageName());
        pkg.setPrice(dto.getPrice());
        pkg.setDayNum(dto.getDayNum());
        pkg.setGivePoint(dto.getGivePoint());
        pkg.setSort(dto.getSort() != null ? dto.getSort() : 0);
        pkg.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        memberPackageService.addPackage(pkg);
        return Result.success();
    }

    /**
     * 编辑套餐
     */
    @PostMapping("/api/admin/member/package/update")
    @OperationLog(module = "营销管理", description = "编辑会员套餐")
    public Result<Void> update(@Valid @RequestBody PackageUpdateDTO dto) {
        MemberPackage pkg = new MemberPackage();
        pkg.setId(dto.getId());
        pkg.setPackageName(dto.getPackageName());
        pkg.setPrice(dto.getPrice());
        pkg.setDayNum(dto.getDayNum());
        pkg.setGivePoint(dto.getGivePoint());
        pkg.setSort(dto.getSort());
        pkg.setStatus(dto.getStatus());
        memberPackageService.updatePackage(pkg);
        return Result.success();
    }

    /**
     * 删除套餐
     */
    @PostMapping("/api/admin/member/package/delete")
    @OperationLog(module = "营销管理", description = "删除会员套餐")
    public Result<Void> delete(@RequestParam Long id) {
        memberPackageService.deletePackage(id);
        return Result.success();
    }

    /**
     * 启用禁用
     */
    @PostMapping("/api/admin/member/package/status")
    @OperationLog(module = "营销管理", description = "修改会员套餐状态")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        memberPackageService.updateStatus(id, status);
        return Result.success();
    }

    // ===== DTO =====

    @Data
    public static class MemberOrderDTO {
        @NotNull(message = "套餐ID不能为空")
        private Long packageId;
    }

    @Data
    public static class PayDTO {
        @NotNull(message = "订单ID不能为空")
        private Long orderId;
    }

    @Data
    public static class PackageDTO {
        @NotBlank(message = "套餐名称不能为空")
        private String packageName;
        @NotNull(message = "价格不能为空")
        private BigDecimal price;
        @NotNull(message = "天数不能为空")
        @Min(value = 1, message = "天数最少1天")
        private Integer dayNum;
        @NotNull(message = "赠送积分不能为空")
        @Min(value = 0, message = "赠送积分不能为负")
        private Integer givePoint;
        private Integer sort;
        private Integer status;
    }

    @Data
    public static class PackageUpdateDTO extends PackageDTO {
        @NotNull(message = "ID不能为空")
        private Long id;
    }
}
