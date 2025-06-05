package com.springai.text.prompttemplate;

import com.springai.services.OpenAiService;
import com.springai.text.prompttemplate.dto.InterviewTips;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class InterviewHelperController {
    @Autowired
    private OpenAiService chatService;

    @GetMapping("/showInterviewHelper")
    public String showChatPage() {
        return "interviewHelper";
    }

    @PostMapping("/interviewHelper")
    public String getChatResponse(@RequestParam("company") String company, @RequestParam("jobTitle") String jobTitle,
                                  @RequestParam("strength") String strength, @RequestParam("weakness") String weakness, Model model) {
        InterviewTips interviewTips = chatService.getInterviewTips(company, jobTitle, strength, weakness);
        model.addAttribute("interviewTips", interviewTips);

        return "interviewHelper";
    }
}
