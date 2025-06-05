package com.springai.text.prompttemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.springai.services.OpenAiService;

@Controller
public class TravelGuideController {
	@Autowired
	private OpenAiService chatService;

	@GetMapping("/showTravelGuide")
	public String showChatPage() {
		return "travelGuide";
	}

	@PostMapping("/travelGuide")
	public String getChatResponse(@RequestParam("city") String city, @RequestParam("month") String month,
			@RequestParam("language") String language, @RequestParam("budget") String budget, Model model) {

		String response = chatService.getTravelGuidance(city, month, language, budget);

		model.addAttribute("response", response);
		model.addAttribute("city", city);
		return "travelGuide";
	}

}
