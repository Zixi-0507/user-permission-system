package com.tonpower.userservice.controller;

import com.tonpower.userservice.common.BaseResponse;
import com.tonpower.userservice.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 86166
 * @ClassName HelloController
 * @Description TODO
 * @date 2025-06-12 18:38
 */
@RestController
@RequestMapping("/test")
public class HelloController {
    @GetMapping("/hello")
    public BaseResponse<String> hello() {
        return ResultUtils.success("hello");
    }
}
