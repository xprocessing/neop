package com.gongziyu.neop.controller.task;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.gongziyu.neop.annotation.OperationLog;
import com.gongziyu.neop.annotation.RateLimit;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.common.Result;
import com.gongziyu.neop.entity.TaskInfo;
import com.gongziyu.neop.entity.TaskPayLog;
import com.gongziyu.neop.service.TaskService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 任务模块接口
 */
@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    // ===== 前台接口 =====

    @GetMapping("/api/task/list")
    public Result<IPage<Map<String, Object>>> frontList(PageDTO pageDTO) {
        return Result.success(taskService.frontList(pageDTO));
    }

    @GetMapping("/api/task/info/{taskId}")
    public Result<Map<String, Object>> frontInfo(@PathVariable Long taskId) {
        return Result.success(taskService.frontInfo(taskId));
    }

    @PostMapping("/api/task/receive")
    @RateLimit(key = "task:receive", limit = 2, period = 1)
    public Result<Map<String, Object>> receive(@RequestBody TaskIdDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = taskService.receiveTask(userId, dto.getTaskId());
        return Result.success(result);
    }

    @PostMapping("/api/task/submit")
    @RateLimit(key = "task:submit", limit = 2, period = 1)
    public Result<Void> submit(@Valid @RequestBody TaskSubmitDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        taskService.submitTask(userId, dto.getReceiveId(), dto.getSubmitImages(), dto.getSubmitNote());
        return Result.success();
    }

    @GetMapping("/api/task/my/list")
    public Result<IPage<Map<String, Object>>> myTaskList(PageDTO pageDTO,
                                                          @RequestParam(required = false) Integer auditStatus,
                                                          HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(taskService.myTaskList(userId, pageDTO, auditStatus));
    }

    @PostMapping("/api/task/withdraw/apply")
    @RateLimit(key = "task:withdraw", limit = 3, period = 60)
    public Result<Map<String, Object>> withdrawApply(@RequestBody ReceiveIdDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Map<String, Object> result = taskService.withdrawApply(userId, dto.getReceiveId());
        return Result.success(result);
    }

    @GetMapping("/api/task/withdraw/log")
    public Result<IPage<TaskPayLog>> withdrawLog(PageDTO pageDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(taskService.withdrawLog(userId, pageDTO));
    }

    // ===== 后台接口 =====

    @GetMapping("/api/admin/task/list")
    @OperationLog(module = "任务管理", description = "查询任务列表")
    public Result<IPage<TaskInfo>> adminList(PageDTO pageDTO,
                                               @RequestParam(required = false) String taskTitle,
                                               @RequestParam(required = false) Integer status) {
        return Result.success(taskService.adminList(pageDTO, taskTitle, status));
    }

    @GetMapping("/api/admin/task/info/{id}")
    public Result<TaskInfo> adminInfo(@PathVariable Long id) {
        return Result.success(taskService.getById(id));
    }

    @PostMapping("/api/admin/task/save")
    @OperationLog(module = "任务管理", description = "新增任务")
    public Result<Void> save(@Valid @RequestBody TaskDTO dto) {
        TaskInfo task = new TaskInfo();
        copyDto(dto, task);
        taskService.saveTask(task);
        return Result.success();
    }

    @PostMapping("/api/admin/task/update")
    @OperationLog(module = "任务管理", description = "编辑任务")
    public Result<Void> update(@Valid @RequestBody TaskUpdateDTO dto) {
        TaskInfo task = new TaskInfo();
        task.setId(dto.getId());
        copyDto(dto, task);
        taskService.updateTask(task);
        return Result.success();
    }

    @PostMapping("/api/admin/task/delete")
    @OperationLog(module = "任务管理", description = "删除任务")
    public Result<Void> delete(@RequestParam Long id) {
        taskService.deleteTask(id);
        return Result.success();
    }

    @PostMapping("/api/admin/task/status")
    @OperationLog(module = "任务管理", description = "任务上下架")
    public Result<Void> status(@RequestParam Long id, @RequestParam Integer status) {
        taskService.updateTaskStatus(id, status);
        return Result.success();
    }

    @GetMapping("/api/admin/task/audit/list")
    @OperationLog(module = "任务管理", description = "查询待审核列表")
    public Result<IPage<Map<String, Object>>> auditList(PageDTO pageDTO,
                                                          @RequestParam(required = false) Integer auditStatus) {
        return Result.success(taskService.auditList(pageDTO, auditStatus));
    }

    @PostMapping("/api/admin/task/audit/pass")
    @OperationLog(module = "任务管理", description = "审核通过")
    public Result<Void> auditPass(@RequestBody ReceiveIdDTO dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        taskService.auditPass(dto.getReceiveId(), adminId);
        return Result.success();
    }

    @PostMapping("/api/admin/task/audit/reject")
    @OperationLog(module = "任务管理", description = "审核驳回")
    public Result<Void> auditReject(@Valid @RequestBody AuditRejectDTO dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        taskService.auditReject(dto.getReceiveId(), adminId, dto.getAuditNote());
        return Result.success();
    }

    @PostMapping("/api/admin/task/grant/pay")
    @OperationLog(module = "任务管理", description = "授权打款")
    public Result<Void> grantPay(@RequestBody ReceiveIdDTO dto, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        taskService.grantPay(dto.getReceiveId(), adminId);
        return Result.success();
    }

    @GetMapping("/api/admin/task/pay/log")
    @OperationLog(module = "任务管理", description = "查询打款日志")
    public Result<IPage<TaskPayLog>> payLogList(PageDTO pageDTO,
                                                  @RequestParam(required = false) Integer payStatus) {
        return Result.success(taskService.payLogList(pageDTO, payStatus));
    }

    // ===== DTO =====

    @Data
    public static class TaskIdDTO {
        @NotNull(message = "任务ID不能为空") private Long taskId;
    }

    @Data
    public static class ReceiveIdDTO {
        @NotNull(message = "领取记录ID不能为空") private Long receiveId;
    }

    @Data
    public static class TaskSubmitDTO {
        @NotNull(message = "领取记录ID不能为空") private Long receiveId;
        @NotBlank(message = "提交截图不能为空") private String submitImages;
        private String submitNote;
    }

    @Data
    public static class AuditRejectDTO {
        @NotNull(message = "领取记录ID不能为空") private Long receiveId;
        @NotBlank(message = "驳回原因不能为空") private String auditNote;
    }

    @Data
    public static class TaskDTO {
        @NotBlank(message = "任务标题不能为空") private String taskTitle;
        private String taskCover;
        private String taskIntro;
        private String taskContent;
        @NotNull(message = "奖励金额不能为空") @DecimalMin("0.01") private BigDecimal rewardAmount;
        @NotNull(message = "总数量不能为空") @Min(0) private Integer totalNum;
        @NotNull(message = "每日限领不能为空") @Min(0) private Integer dayNum;
        @NotNull(message = "过期时间不能为空") @Min(1) private Integer expireMinute;
        private Integer sort;
        private Integer status;
    }

    @Data
    public static class TaskUpdateDTO extends TaskDTO {
        @NotNull(message = "ID不能为空") private Long id;
    }

    private void copyDto(TaskDTO dto, TaskInfo task) {
        task.setTaskTitle(dto.getTaskTitle());
        task.setTaskCover(dto.getTaskCover());
        task.setTaskIntro(dto.getTaskIntro());
        task.setTaskContent(dto.getTaskContent());
        task.setRewardAmount(dto.getRewardAmount());
        task.setTotalNum(dto.getTotalNum());
        task.setDayNum(dto.getDayNum());
        task.setExpireMinute(dto.getExpireMinute());
        task.setSort(dto.getSort() != null ? dto.getSort() : 0);
        task.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
    }
}
