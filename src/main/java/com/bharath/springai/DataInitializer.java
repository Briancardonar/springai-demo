package com.bharath.springai;

import jakarta.annotation.PostConstruct;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer {

    @Autowired
    private VectorStore vectorStore;

    @PostConstruct
    public void init() {
        TokenTextSplitter splitter = new TokenTextSplitter(100, 100, 5, 1000, true);

        TextReader jobsText = new TextReader(new ClassPathResource("job_listings.txt"));
        List<Document> documents = splitter.split(jobsText.get());

        vectorStore.add(documents);

        TextReader productText = new TextReader(new ClassPathResource("product-data.txt"));
        documents = splitter.split(productText.get());
        vectorStore.add(documents);
    }
}
