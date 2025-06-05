package com.springai.imageprocessing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.springai.services.OpenAiService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

@Controller
public class ImageAnalyzerController {

    private static final String UPLOAD_DIR = "/Users/brian/Documents/springai/";

    @Autowired
    private OpenAiService service;

    // Display the image upload form
    @GetMapping("showImageAnalyzer")
    public String showUploadForm() {
        return "imageAnalyzer";
    }

    @PostMapping("/imageAnalyzer")
    public String uploadImage(String prompt, @RequestParam("file") MultipartFile file, Model model, RedirectAttributes redirectAttributes) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR);

            if (Files.notExists(filePath)) {
                Files.createDirectories(filePath);
            }

            Path path = filePath.resolve(Objects.requireNonNull(file.getOriginalFilename()));
            Files.write(path, file.getBytes(), StandardOpenOption.CREATE);

            String response = service.explainImage(prompt, path.toString());
            model.addAttribute("explanation", response);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("message", e.getMessage());
        }

        return "imageAnalyzer";
    }
}