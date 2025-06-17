package com.tonpower.loggingservice.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 
 * @TableName operation_logs
 */
@TableName(value ="operation_logs")
@Data
public class OperationLogs {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long logId;

    /**
     * 
     */
    private Long userId;

    /**
     * 
     */
    private String action;

    /**
     * 
     */
    private String ip;

    /**
     * 
     */
    private String detail;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 更新时间
     */
    private Date gmtModified;

    /**
     * 是否删除
     */
    private Integer isDeleted;
}