package com.gongziyu.neop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gongziyu.neop.common.PageDTO;
import com.gongziyu.neop.entity.TaskInfo;
import com.gongziyu.neop.entity.TaskPayLog;
import com.gongziyu.neop.entity.TaskUserReceive;

import java.util.Map;

public interface TaskService extends IService<TaskInfo> {

    // 前台接口
    IPage<Map<String, Object>> frontList(PageDTO pageDTO, String keyword);
    Map<String, Object> frontInfo(Long taskId);
    Map<String, Object> receiveTask(Long userId, Long taskId);
    void submitTask(Long userId, Long receiveId, String submitImages, String submitNote);
    IPage<Map<String, Object>> myTaskList(Long userId, PageDTO pageDTO, Integer auditStatus);
    Map<String, Object> withdrawApply(Long userId, Long receiveId);
    IPage<TaskPayLog> withdrawLog(Long userId, PageDTO pageDTO);

    // 后台接口
    IPage<TaskInfo> adminList(PageDTO pageDTO, String taskTitle, Integer status);
    void saveTask(TaskInfo taskInfo);
    void updateTask(TaskInfo taskInfo);
    void deleteTask(Long id);
    void updateTaskStatus(Long id, Integer status);
    IPage<Map<String, Object>> auditList(PageDTO pageDTO, Integer auditStatus);
    void auditPass(Long receiveId, Long adminId);
    void auditReject(Long receiveId, Long adminId, String auditNote);
    void grantPay(Long receiveId, Long adminId);
    IPage<TaskPayLog> payLogList(PageDTO pageDTO, Integer payStatus);
}
