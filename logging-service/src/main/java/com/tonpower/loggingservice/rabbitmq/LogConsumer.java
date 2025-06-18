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
            OperationLogs log = new OperationLogs();
            JSONObject data = JSONUtil.parseObj(message);
            String action = data.getStr("action");
            Long userId = data.getLong("userId");
            String ip = data.getStr("ip");
            //如果data中有detail字段，则保存detail字段的值
            if(data.containsKey("detail")){
                String detail=data.getStr("detail");
                log.setDetail(detail);
            }else{
                log.setDetail(message);
            }
            log.setAction(action);
            log.setUserId(userId);
            log.setIp(ip);

            operationLogsService.save(log);
            System.out.println("Saved log: " + message);
        };
    }
}
