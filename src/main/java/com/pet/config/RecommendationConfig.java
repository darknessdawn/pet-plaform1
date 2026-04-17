package com.pet.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationConfig {
    
    private Double userCfWeight;
    private Double itemCfWeight;
    private Double svdWeight;
    private Double similarityThreshold;
    private Integer maxRecommendations;
    private Integer svdDimensions;
}
