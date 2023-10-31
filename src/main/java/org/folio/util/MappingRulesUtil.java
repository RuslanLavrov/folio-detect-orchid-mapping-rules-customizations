package org.folio.util;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class MappingRulesUtil {

    public static Map<String, JsonNode> getChangedMappingRules(JsonNode originalMappingRules, JsonNode actualMappingRules) {
        Map<String, JsonNode> changedRules = new LinkedHashMap<>();
        originalMappingRules.fields().forEachRemaining(marcFieldRule -> {
            JsonNode actualMarcFieldRule = actualMappingRules.get(marcFieldRule.getKey());
            if (!marcFieldRule.getValue().equals(actualMarcFieldRule)) {
                changedRules.put(marcFieldRule.getKey(), actualMarcFieldRule);
            }
        });
        return changedRules;
    }

}
