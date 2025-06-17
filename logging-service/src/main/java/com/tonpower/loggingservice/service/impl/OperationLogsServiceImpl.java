package com.tonpower.loggingservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tonpower.loggingservice.model.entity.OperationLogs;
import com.tonpower.loggingservice.service.OperationLogsService;
import com.tonpower.loggingservice.mapper.OperationLogsMapper;
import org.springframework.stereotype.Service;

/**
* @author 86166
* @description 针对表【operation_logs】的数据库操作Service实现
* @createDate 2025-06-17 10:44:24
*/
@Service
public class OperationLogsServiceImpl extends ServiceImpl<OperationLogsMapper, OperationLogs>
    implements OperationLogsService{

}




