package com.kuney.community.application.controller;


import com.google.code.kaptcha.Producer;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.Constants;
import com.kuney.community.util.ObjCheckUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Controller
@RequestMapping("/user")
@AllArgsConstructor
@Slf4j
public class UserController {

    private UserService userService;
    private Producer kaptchaProducer;

    @GetMapping("register")
    public String toRegister() {
        return "site/register";
    }

    @PostMapping("register")
    public String register(@Validated User user, Model model) {
        int result = userService.userRegister(user);
        if (result == 0) {
            model.addAttribute("msg", "注册成功！我们已经向您发送了一份激活邮件，请尽快激活！");
            model.addAttribute("target", "/");
            return "site/operate-result";
        }
        model.addAttribute("resultCode", result);
        return "site/register";
    }

    @GetMapping("activation/{userId}/{activationCode}")
    public String activation(@PathVariable("userId") Integer userId, Model model,
                             @PathVariable("activationCode") String activationCode) {
        int result = userService.userActivation(userId, activationCode);
        if (result == Constants.UserActivation.REPEAT) {
            model.addAttribute("msg", "无效操作，请勿重复激活！");
        } else if (result == Constants.UserActivation.FAIL) {
            model.addAttribute("msg", "激活失败，无效的激活码！");
        } else {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用！");
        }
        model.addAttribute("target", "/");
        return "site/operate-result";
    }

    @GetMapping("login")
    public String toLogin() {
        return "site/login";
    }

    @PostMapping("login")
    public String login(String username, String password, String code, Boolean rememberMe,
                        Model model, HttpSession session, HttpServletResponse response) {
        String loginCode = (String) session.getAttribute("loginCode");
        if (ObjCheckUtils.isBlank(loginCode) || !loginCode.equalsIgnoreCase(code)) {
            model.addAttribute("resultCode", Constants.Login.CODE_ERROR);
            return "site/login";
        }
        int expireSeconds = rememberMe != null && rememberMe ?
        Constants.Login.REMEMBER_EXPIRE_SECONDS : Constants.Login.DEFAULT_EXPIRE_SECONDS;
        Map<String, Object> map = userService.userLogin(username, password, expireSeconds);
        if (map.containsKey("ticket")) {
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setMaxAge(expireSeconds);
            cookie.setPath(session.getServletContext().getContextPath());
            response.addCookie(cookie);
            return "redirect:/";
        }
        model.addAttribute("resultCode", map.get("resultCode"));
        return "site/login";
    }

    @GetMapping("logout")
    public String logout(@CookieValue("ticket") String ticket) {
        userService.userLogout(ticket);
        return "redirect:/";
    }

    @GetMapping("code")
    public void getLoginCode(HttpSession session, HttpServletResponse response) {
        // 生成验证码
        String code = kaptchaProducer.createText();
        // 验证码图片
        BufferedImage image = kaptchaProducer.createImage(code);
        session.setAttribute("loginCode", code);
        response.setContentType("image/png");
        try {
            ImageIO.write(image, "png", response.getOutputStream());
        } catch (IOException e) {
            log.error("获取验证码失败：{}", e.getMessage());
            e.printStackTrace();
        }
    }
}

