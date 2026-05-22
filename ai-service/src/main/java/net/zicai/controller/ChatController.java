package net.zicai.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author wangdi
 * @date 2026/5/1 15:46
 * @description
 */
@RestController
@RequestMapping("api/v1/ai")
@RequiredArgsConstructor
public class ChatController {

    private final ChatClient chatClient;


    @GetMapping("/chat1")
    public String chat1(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}