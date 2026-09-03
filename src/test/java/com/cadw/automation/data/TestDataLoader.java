package com.cadw.automation.data;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public final class TestDataLoader {
    private static final String SEARCH_DATA = "data/search-data.json";
    private static final Map<String, SearchScenario> SCENARIOS = loadSearchData();

    private TestDataLoader() {
    }

    public static SearchScenario searchScenario(String id) {
        SearchScenario scenario = SCENARIOS.get(id);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown search data id: " + id);
        }
        return scenario;
    }

    private static Map<String, SearchScenario> loadSearchData() {
        try (InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(SEARCH_DATA)) {
            if (stream == null) {
                throw new IllegalStateException("Test data not found: " + SEARCH_DATA);
            }
            return new ObjectMapper().readValue(stream, new TypeReference<>() {
            });
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + SEARCH_DATA, exception);
        }
    }
}
