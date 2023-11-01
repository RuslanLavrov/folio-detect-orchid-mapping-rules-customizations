package org.folio.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.Map;

public class MappingRulesUtilTest {

    @Test
    public void shouldReturnDifferentMappingRules() {
        ObjectNode rulesToCompare = FileWorker.getJsonObject("rules/poppyClassificationFieldMappingRules.json");
        ObjectNode mappingRulesWithChanges = FileWorker.getJsonObject("rules/rules_with_differences_in_050_060_082.json");

        Map<String, JsonNode> differentRules = MappingRulesUtil.getChangedMappingRules(rulesToCompare, mappingRulesWithChanges);

        MatcherAssert.assertThat(differentRules.keySet(), Matchers.contains("050", "060", "082"));
    }

}