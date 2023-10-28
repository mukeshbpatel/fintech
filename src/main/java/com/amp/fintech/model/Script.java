package com.amp.fintech.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

@Builder
@Getter
@Setter
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class Script implements Serializable {
    private final String name;
    private final String apiKey;
    private final String startDate;
    private final String endDate;
    private final String timeFrame;
    private final boolean fillTechnicalAnalysis;
}
