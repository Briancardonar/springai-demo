package com.springai.text;

import com.springai.services.OpenAiService;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AnswerAnyThingController {

    @Autowired
    private OpenAiService chatService;

    @GetMapping("/showAskAnything")
    public String showAskAnything() {
        return "askAnything";
    }

    @PostMapping("/askAnything")
    public String askAnything(@RequestParam("question") String question, Model model) {
        ChatResponse chatResponse = chatService.generateResponse(question);
        System.out.println(chatResponse);

        model.addAttribute("question", question);
        model.addAttribute("answer", chatResponse.getResult().getOutput().getText());

        return "askAnything";
    }
}