package com.kuney.community.application.controller;


import com.kuney.community.annotation.LoginRequired;
import com.kuney.community.application.entity.Message;
import com.kuney.community.application.entity.User;
import com.kuney.community.application.service.MessageService;
import com.kuney.community.util.HostHolder;
import com.kuney.community.util.Result;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author kuneychen
 * @since 2022-06-10
 */
@Controller
@RequestMapping("/message")
@AllArgsConstructor
public class MessageController {

    private MessageService messageService;
    private HostHolder hostHolder;


    @LoginRequired
    @GetMapping
    public String getLetterPage(@RequestParam(required = false, defaultValue = "1") int pageNum, Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> data = messageService.getMessageIndex(pageNum, user.getId());
        model.addAttribute("page", data.get("page"));
        model.addAttribute("pageBegin", data.get("pageBegin"));
        model.addAttribute("pageEnd", data.get("pageEnd"));
        model.addAttribute("unreadLetter", messageService.countUnreadLetter(user.getId()));
        model.addAttribute("unreadNotice", messageService.countUnreadNotice(user.getId()));
        model.addAttribute("path", "/message");
        return "site/letter";
    }

    @LoginRequired
    @GetMapping("{conversationId}")
    public String letterDetail(@PathVariable String conversationId,
                               @RequestParam(required = false, defaultValue = "1") int pageNum,
                               Model model) {
        Map<String, Object> result = messageService.letterDetailPage(conversationId, pageNum);
        model.addAttribute("page", result.get("page"));
        model.addAttribute("pageBegin", result.get("pageBegin"));
        model.addAttribute("pageEnd", result.get("pageEnd"));
        model.addAttribute("target", result.get("target"));
        model.addAttribute("path", "/message/" + conversationId);
        return "site/letter-detail";
    }

    @LoginRequired
    @PostMapping
    @ResponseBody
    public Result sendMessage(String username, String content) {
        messageService.saveMessage(username, content);
        return Result.success("发送成功");
    }

    @LoginRequired
    @PostMapping("delete/{messageId}")
    @ResponseBody
    public Result deleteMessage(@PathVariable Integer messageId) {
        messageService.lambdaUpdate()
                .set(Message::getStatus, 2)
                .eq(Message::getId, messageId)
                .update();
        return Result.success("删除成功");
    }

    @LoginRequired
    @GetMapping("notice")
    public String getNoticePage(Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> data = messageService.getNoticeList(user.getId());
        int unreadNotice = messageService.countUnreadNotice(user.getId());
        int unreadLetter = messageService.countUnreadLetter(user.getId());

        model.addAttribute("commentNotice", data.get("commentNotice"));
        model.addAttribute("likeNotice", data.get("likeNotice"));
        model.addAttribute("followNotice", data.get("followNotice"));
        model.addAttribute("unreadNotice", unreadNotice);
        model.addAttribute("unreadLetter", unreadLetter);
        return "site/notice";
    }

    @LoginRequired
    @GetMapping("notice/{topic}")
    public String noticeDetail(@PathVariable String topic, Model model,
                               @RequestParam(required = false, defaultValue = "1") int pageNum) {

        User user = hostHolder.getUser();
        Map<String, Object> result = messageService.getNoticeDetail(topic, pageNum, user.getId());
        model.addAttribute("page", result.get("page"));
        model.addAttribute("pageBegin", result.get("pageBegin"));
        model.addAttribute("pageEnd", result.get("pageEnd"));
        model.addAttribute("path", "/message/notice/" + topic);
        return "site/notice-detail";
    }

}

