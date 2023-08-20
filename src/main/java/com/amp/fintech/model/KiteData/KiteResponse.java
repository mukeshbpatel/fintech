package com.amp.fintech.model.KiteData;

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
public class KiteResponse implements Serializable {
    private final String status;
    private final Data data;

}
