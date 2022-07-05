package com.kuney.community.application.controller;

import com.kuney.community.annotation.LoginRequired;
import com.kuney.community.annotation.Permission;
import com.kuney.community.application.service.DataService;
import com.kuney.community.util.Constants.Role;
import com.kuney.community.util.Result;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * @author kuneychen
 * @since 2022/7/5 21:02
 */
@RequestMapping("/data")
@Controller
@AllArgsConstructor
@Validated
public class DataController {

    private DataService dataService;

    @Permission(role = Role.ADMIN)
    @LoginRequired
    @GetMapping
    public String index() {
        return "site/admin/data";
    }

    @Permission(role = Role.ADMIN)
    @LoginRequired
    @PostMapping("uv")
    @ResponseBody
    public Result getUV(@NotNull(message = "起始时间为空") LocalDate begin,
                        @NotNull(message = "结束时间为空") LocalDate end) {
        long result = dataService.getUV(begin, end);
        return Result.data(result);
    }

    @Permission(role = Role.ADMIN)
    @LoginRequired
    @PostMapping("dau")
    @ResponseBody
    public Result getDAU(@NotNull(message = "起始时间为空") LocalDate begin,
                         @NotNull(message = "结束时间为空") LocalDate end) {
        long result = dataService.getDAU(begin, end);
        return Result.data(result);
    }
}
