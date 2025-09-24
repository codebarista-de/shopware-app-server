package de.codebarista.shopware.appbackend.sdk.service.dto.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class SearchResult<T> {
    @JsonProperty("total")
    private int total;

    @JsonProperty("data")
    private List<T> data;

    public int total() {
        return total;
    }

    public List<T> data() {
        return data;
    }
}
