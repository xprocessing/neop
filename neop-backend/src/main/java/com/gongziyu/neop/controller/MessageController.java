package com.gongziyu.neop.controller;

import com.gongziyu.neop.common.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam(defaultValue = "1") Integer page,
                                             @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(Map.of("records", List.of(), "total", 0));
    }

    @PostMapping("/read/{id}")
    public Result<Void> read(@PathVariable Long id) {
        return Result.success();
    }

    @PostMapping("/read-all")
    public Result<Void> readAll() {
        return Result.success();
    }
}
