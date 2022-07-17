package com.kuney.community.application.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.code.kaptcha.Producer;
import com.kuney.community.annotation.LoginRequired;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.FollowService;
import com.kuney.community.application.service.LikeService;
import com.kuney.community.application.service.UserService;
import com.kuney.community.util.*;
import com.kuney.community.util.Constants.Login;
import com.kuney.community.util.Constants.Register;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Controller
@RequestMapping("/user")
@Slf4j
@Validated
public class UserController {

    @Value("${user.image.path}")
    private String imagePath;

    private UserService userService;
    private Producer kaptchaProducer;
    private CommunityUtils communityUtils;
    private HostHolder hostHolder;
    private LikeService likeService;
    private FollowService followService;
    private RedisTemplate redisTemplate;
    private TemplateEngine templateEngine;
    private ThreadPoolExecutor executor;

    public UserController(UserService userService, Producer kaptchaProducer, CommunityUtils communityUtils, HostHolder hostHolder, LikeService likeService,
                          FollowService followService, RedisTemplate redisTemplate, TemplateEngine templateEngine, ThreadPoolExecutor executor) {
        this.userService = userService;
        this.kaptchaProducer = kaptchaProducer;
        this.communityUtils = communityUtils;
        this.hostHolder = hostHolder;
        this.likeService = likeService;
        this.followService = followService;
        this.redisTemplate = redisTemplate;
        this.templateEngine = templateEngine;
        this.executor = executor;
    }

    @GetMapping("register")
    public String toRegister() {
        return "site/register";
    }

    @PostMapping("register")
    public String register(@Validated User user, Model model) {
        int result = userService.userRegister(user);
        if (result == Register.SUCCESS) {
            model.addAttribute("msg", "注册成功！我们已经向您发送了一份激活邮件，请尽快激活！");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        }
        model.addAttribute("resultCode", result);
        return "site/register";
    }

    @GetMapping("forget")
    public String forget() {
        return "site/forget";
    }

    @GetMapping("forget/code")
    @ResponseBody
    public Result getCode(@Email String email) {
        User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getEmail, email));
        if (ObjCheckUtils.isNull(user)) {
            return Result.fail("邮箱错误，或该邮箱未注册！");
        }
        String key = RedisKeyUtils.getForgetKey(email);
        if (redisTemplate.opsForValue().get(key) != null) {
            return Result.fail("获取验证码太频繁，请稍后再获取！");
        }
        String code = EncodeUtils.generateCode();
        CompletableFuture.runAsync(() -> {
            Context ctx = new Context();
            ctx.setVariable("username", email);
            ctx.setVariable("code", code);
            String content = templateEngine.process("mail/forget", ctx);
            communityUtils.sendSimpleMail(email, "忘记密码", content);
        }, executor);
        redisTemplate.opsForValue().set(key, code, 2, TimeUnit.MINUTES);
        return Result.success("验证码已发送至邮箱，请注意查收！");
    }

    @PostMapping("forget/reset")
    public String resetPassword(@Email String email, @NotBlank String code, @NotBlank String password, Model model) {
        User user = userService.getOne(Wrappers.<User>lambdaQuery().eq(User::getEmail, email));
        if (ObjCheckUtils.isNull(user)) {
            model.addAttribute("emailMsg", "该账号未注册");
            return "site/forget";
        }
        String key = RedisKeyUtils.getForgetKey(email);
        Object storeCode = redisTemplate.opsForValue().get(key);
        if (ObjCheckUtils.isNull(storeCode)) {
            model.addAttribute("msg", "验证码已失效");
            return "site/forget";
        }
        if (!storeCode.equals(code)) {
            model.addAttribute("msg", "验证码不正确");
            return "site/forget";
        }
        userService.lambdaUpdate()
                .eq(User::getEmail, email)
                .set(User::getPassword, EncodeUtils.encodePassword(password, user.getSalt())).update();
        model.addAttribute("msg", "密码重置成功，您可以前去登录了！");
        model.addAttribute("target", "/user/login");
        return "site/operate-result";
    }

    @GetMapping("activation/{userId}/{activationCode}")
    public String activation(@PathVariable("userId") Integer userId, Model model,
                             @PathVariable("activationCode") String activationCode) {
        int result = userService.userActivation(userId, activationCode);
        if (result == Constants.Activation.REPEAT) {
            model.addAttribute("msg", "无效操作，请勿重复激活！");
        } else if (result == Constants.Activation.FAIL) {
            model.addAttribute("msg", "激活失败，无效的激活码！");
        } else {
            model.addAttribute("msg", "激活成功，您的账号已经可以正常使用！");
        }
        model.addAttribute("target", "/index");
        return "site/operate-result";
    }

    @GetMapping("login")
    public String toLogin() {
        return "site/login";
    }

    @PostMapping("login")
    public String login(String username, String password, String code, Boolean rememberMe,
                        Model model, HttpServletResponse response, HttpSession session,
                        @CookieValue("codeOwner") String codeOwner) {
        if (ObjCheckUtils.isBlank(codeOwner)) {
            model.addAttribute("resultCode", Login.CODE_ERROR);
            model.addAttribute("codeMsg", "验证码已失效！");
            return "site/login";
        }
        Object loginCode = redisTemplate.opsForValue().get(RedisKeyUtils.getLoginCodeKey(codeOwner));
        if (ObjCheckUtils.isNull(loginCode) || !((String) loginCode).equalsIgnoreCase(code)) {
            model.addAttribute("resultCode", Login.CODE_ERROR);
            model.addAttribute("codeMsg", "验证码不正确！");
            return "site/login";
        }
        int expireSeconds = rememberMe != null && rememberMe ?
                Login.REMEMBER_EXPIRE_SECONDS : Login.DEFAULT_EXPIRE_SECONDS;
        Map<String, Object> map = userService.userLogin(username, password, expireSeconds);
        if (map.containsKey("token")) {
            Cookie cookie = new Cookie("token", (String) map.get("token"));
            cookie.setMaxAge(expireSeconds);
            cookie.setPath(session.getServletContext().getContextPath());
            response.addCookie(cookie);
            return "redirect:/index";
        }
        model.addAttribute("resultCode", map.get("resultCode"));
        return "site/login";
    }

    @GetMapping("logout")
    public String logout(@CookieValue("token") String token) {
        userService.userLogout(token);
        return "redirect:/index";
    }

    @GetMapping("code")
    public void getLoginCode(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String code = kaptchaProducer.createText();
        // 验证码图片
        BufferedImage image = kaptchaProducer.createImage(code);
        // session.setAttribute("loginCode", code);
        String codeOwner = EncodeUtils.generateUUID();
        redisTemplate.opsForValue().set(RedisKeyUtils.getLoginCodeKey(codeOwner), code, 60, TimeUnit.SECONDS);
        Cookie cookie = new Cookie("codeOwner", codeOwner);
        cookie.setPath(session.getServletContext().getContextPath());
        response.addCookie(cookie);
        response.setContentType("image/png");
        try {
            ImageIO.write(image, "png", response.getOutputStream());
        } catch (IOException e) {
            log.error("获取验证码失败：{}", e.getMessage());
        }
    }

    @LoginRequired
    @GetMapping("setting")
    public String setting() {
        return "site/setting";
    }

    @LoginRequired
    @PostMapping("upload")
    public String upload(MultipartFile headImage, Model model) {
        if (ObjCheckUtils.isNull(headImage)) {
            model.addAttribute("uploadMsg", "请选择一个文件");
            return "site/setting";
        }
        String originalFilename = headImage.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (ObjCheckUtils.isBlank(suffix) || !Arrays.asList(".png", ".jpg", ".jpeg").contains(suffix)) {
            model.addAttribute("uploadMsg", "文件格式错误！仅支持.png, .jpg, .jpeg格式文件");
            return "site/setting";
        }
        String url = communityUtils.uploadFile(headImage, suffix);
        userService.lambdaUpdate()
                .set(User::getHeaderUrl, url)
                .eq(User::getId, hostHolder.getUser().getId())
                .update();
        model.addAttribute("msg", "头像上传成功！");
        model.addAttribute("target", "/index");
        return "site/operate-result";
    }

    @GetMapping("header/{fileName}")
    public void getHeader(@PathVariable String fileName, HttpServletResponse response) {
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        fileName = imagePath + fileName;
        response.setContentType("image/" + suffix);
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(fileName))) {
            ServletOutputStream outputStream = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            log.error("获取头像失败：{}", e.getMessage());
        }
    }

    @LoginRequired
    @PutMapping("password")
    public String updatePassword(@CookieValue("token") String token, Model model,
                                 String oldPassword, String newPassword) {
        User user = hostHolder.getUser();
        if (!StringUtils.equals(user.getPassword(), EncodeUtils.encodePassword(oldPassword, user.getSalt()))) {
            model.addAttribute("msg", "原密码错误！");
            return "site/setting";
        }
        userService.updatePassword(newPassword, token, user);
        model.addAttribute("msg", "密码修改成功，请重新登录！");
        model.addAttribute("target", "/user/login");
        return "site/operate-result";
    }

    @GetMapping("profile/{userId}")
    public String profile(@PathVariable int userId, Model model) {
        User user = userService.getUser(userId);
        int likeCount = likeService.getUserLikeCount(userId);
        long followeeCount = followService.followeeCount(userId, Constants.EntityType.USER);
        long followerCount = followService.followerCount(Constants.EntityType.USER, userId);
        User current = hostHolder.getUser();
        if (current != null) {
            boolean isFollowed = followService.isFollowed(current.getId(), Constants.EntityType.USER, userId);
            model.addAttribute("isFollowed", isFollowed);
        }

        model.addAttribute("user", user);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("followeeCount", followeeCount);
        model.addAttribute("followerCount", followerCount);
        return "site/profile";
    }
}

