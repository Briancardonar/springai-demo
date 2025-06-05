package com.bharath.springai.services;

import com.bharath.springai.text.prompttemplate.dto.CountryCuisines;
import com.bharath.springai.text.prompttemplate.dto.InterviewTips;
import com.bharath.springai.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageOptionsBuilder;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final ChatClient chatClient;

    @Autowired
    private EmbeddingModel embeddingModel;

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private ImageModel imageModel;

    public OpenAiService(ChatClient.Builder builder, ChatMemory chatMemory) {
        chatClient = builder.defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    public ChatResponse generateResponse(String prompt) {
        OpenAiChatOptions options = OpenAiChatOptions.builder().temperature(0.7).build();

        return chatClient.prompt(new Prompt(prompt, options))
                .advisors(ctx -> {
                    ctx.param(ChatMemory.CONVERSATION_ID, "default");
                })
                .call()
                .chatResponse();
    }

    public String getTravelGuidance(String city, String month, String language, String budget) {
        PromptTemplate promptTemplate = PromptTemplate.builder().template(
                "Welcome to the {city} travel guide!\n" +
                        "If you're visiting in {month}, here's what you can do:\n" +
                        "1. Must-visit attractions.\n" +
                        "2. Local cuisine you must try.\n" +
                        "3. Useful phrases in {language}.\n" +
                        "4. Tips for traveling on a {budget} budget.\n" +
                        "Enjoy your trip!"
        ).build();

        Prompt prompt = promptTemplate.create(Map.of("city", city, "month", month, "language", language, "budget", budget));

        return chatClient.prompt(prompt).call().content();
    }

    public CountryCuisines getCuisines(String country, String numCuisines, String language) {
        PromptTemplate promptTemplate = PromptTemplate.builder().template(
                "You are an expert in traditional cuisines. Answer the question: What is the traditional cuisine of {country}? Return a list of {numCuisines} in {language}."
        ).build();

        Prompt prompt = promptTemplate.create(Map.of("country", country, "numCuisines", numCuisines, "language", language));

        return chatClient.prompt(prompt).call().entity(CountryCuisines.class);
    }

    public InterviewTips getInterviewTips(String company, String jobTitle, String strength, String weakness) {
        PromptTemplate promptTemplate = PromptTemplate.builder().template(
                "You are a career coach. Provide tailored interview tips for the\n" +
                        "position of {position} at {company}.\n" +
                        "Highlight your strengths in {strengths} and prepare for questions\n" +
                        "about your weaknesses such as {weaknesses}.").build();

        Prompt prompt = promptTemplate.create(Map.of("company", company, "position", jobTitle, "strengths", strength, "weaknesses", weakness));

        return chatClient.prompt(prompt).call().entity(InterviewTips.class);
    }

    public float[] embed(String text) {
        return embeddingModel.embed(text);
    }

    public double findSimilarity(String text1, String text2) {
        List<float[]> response = embeddingModel.embed(List.of(text1, text2));
        return cosineSimilarity(response.get(0), response.get(1));
    }


    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vectors must be of the same length");
        }

        double dotProduct = 0.0;
        double magnitudeA = 0.0;
        double magnitudeB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            magnitudeA += vectorA[i] * vectorA[i];
            magnitudeB += vectorB[i] * vectorB[i];
        }

        return dotProduct / (Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB));
    }

    public List<Document> searchJobs(String query) {
        return vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(2).build());
    }

    public String answer(String query) {
        return chatClient.prompt(query).advisors(new QuestionAnswerAdvisor(vectorStore)).call().content();
    }

    public String generateImage(String prompt) {
        ImageResponse imageResponse = imageModel.call(new ImagePrompt(prompt, OpenAiImageOptions.builder()
                .quality("hd")
                .height(1024)
                .width(1024)
                .N(1)
                .build()));

        return imageResponse.getResult().getOutput().getUrl();
    }

    public String explainImage(String prompt, String path) {
        return chatClient.prompt().user(
                        u -> u.text(prompt).media(MimeTypeUtils.IMAGE_JPEG, new FileSystemResource(path)))
                .call().content();
    }

    public String callFunction(String query) {
        return chatClient.prompt(query).tools(new WeatherTools()).call().content();
    }
}
