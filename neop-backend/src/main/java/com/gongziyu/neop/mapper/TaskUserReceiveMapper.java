package com.gongziyu.neop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gongziyu.neop.entity.TaskUserReceive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskUserReceiveMapper extends BaseMapper<TaskUserReceive> {

    /**
     * 查询并加行锁（防止并发审核）
     */
    @Select("SELECT * FROM task_user_receive WHERE id = #{receiveId} FOR UPDATE")
    TaskUserReceive selectForUpdate(Long receiveId);
}
