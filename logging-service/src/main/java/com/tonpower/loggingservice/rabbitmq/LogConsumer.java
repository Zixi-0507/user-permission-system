package com.tonpower.loggingservice.rabbitmq;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tonpower.loggingservice.model.entity.OperationLogs;
import com.tonpower.loggingservice.service.OperationLogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;
@RequiredArgsConstructor
@Service
public class LogConsumer {
    private final OperationLogsService operationLogsService;
    @Bean
    public Consumer<String> handleMessage() {
        return message -> {
            JSONObject data = JSONUtil.parseObj(message);
            String action = data.getStr("action");
            Long userId = data.getLong("userId");
            String ip = data.getStr("ip");
            OperationLogs log = new OperationLogs();
            log.setAction(action);
            log.setUserId(userId);
            log.setIp(ip);
            // 原始消息作为 detail
            log.setDetail(message);
            operationLogsService.save(log);
            System.out.println("Saved log: " + message);
        };
    }
}
