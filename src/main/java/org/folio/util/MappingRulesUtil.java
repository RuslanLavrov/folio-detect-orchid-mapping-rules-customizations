package org.folio.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

@Slf4j
public class MappingRulesUtil {
    public static final List<String> contributorsFieldsWithSubfieldEtoUpdate = List.of("100", "110", "700", "710" );
    public static final List<String> contributorsFieldsWithSubfieldJtoUpdate = List.of("111", "711" );
    public static final String ENTITY = "entity";
    public static final String TARGET = "target";
    public static final String SERIES = "series";
    public static final String SUBJECTS = "subjects";
    public static final String CONTRIBUTORS = "contributors";
    public static final String ALTERNATIVE_TITLES = "alternativeTitles";
    public static final String DOT = ".";
    public static final String VALUE = "value";
    public static final String CONTRIBUTOR_TYPE_ID = "contributors.contributorTypeId";
    public static final String CONTRIBUTOR_TYPE_TEXT = "contributors.contributorTypeText";
    public static final String UPDATED_MAPPING_RULES_PATH = "mappingRules/";
    public static final String RELATOR_TERM_E_SUBFIELD_TARGET = "relatorTerm/contributorsFieldsWithSubfieldETarget.json";
    public static final String RELATOR_TERM_J_SUBFIELD_TARGET = "relatorTerm/contributorsFieldsWithSubfieldJTarget.json";
    public static final String ALTERNATIVE_TITLE_TARGET = "authorityControl/alternativeTitlesTarget.json";
    public static final String CONTRIBUTORS_TARGET = "authorityControl/contributorsTarget.json";
    public static final String SERIES_TARGET = "authorityControl/seriesTarget.json";
    public static final String CANNOT_UPDATE_AUTHORITY_CONTROL_LOG = "Cannot update %s mapping rules, field: %s";
    public static final String UPDATING_AUTHORITY_CONTROL_LOG = "Updating %s mapping rules for fields: %s";
    public static final String SUBJECTS_TARGET = "authorityControl/subjectsTarget.json";

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

    public static void replaceMappingRulesForMarcFields(ObjectNode rulesReplacement, ObjectNode targetMappingRules) {
        rulesReplacement.fields().forEachRemaining(marcFieldRule -> {
            targetMappingRules.set(marcFieldRule.getKey(), marcFieldRule.getValue());
            log.info("Mapping rules for MARC field \"{}\" have been updated", marcFieldRule.getKey());
        });
    }

    public static ObjectNode getMappingRulesExcludingByMarcFields(ObjectNode sourceMappingRules, Set<String> marcFieldsToFilter) {
        ObjectNode filteredMappingRules = JsonNodeFactory.instance.objectNode();
        sourceMappingRules.fields().forEachRemaining(marcFieldRule -> {
            if (!marcFieldsToFilter.contains(marcFieldRule.getKey())) {
                filteredMappingRules.set(marcFieldRule.getKey(), marcFieldRule.getValue());
            }
        });
        return filteredMappingRules;
    }

    public void relatorTermUpdate(JsonNode mappingRules) {
        log.info(format("Updating relator term mapping rules, subfield \"e\" for fields: %s", StringUtils.collectionToCommaDelimitedString(contributorsFieldsWithSubfieldEtoUpdate)));
        contributorsFieldsWithSubfieldEtoUpdate.forEach(field -> {
            JsonNode fieldRule = mappingRules.get(field);
            if (fieldRule != null) {
                try {
                    removeTarget(fieldRule, CONTRIBUTOR_TYPE_ID);
                    removeTarget(fieldRule, CONTRIBUTOR_TYPE_TEXT);
                    addTarget(fieldRule, UPDATED_MAPPING_RULES_PATH + RELATOR_TERM_E_SUBFIELD_TARGET);
                } catch (Exception e) {
                    log.warn(format("Cannot update relator term mapping rules, subfield \"e\", field: %s", field), e);
                    throw e;
                }
            }
        });

        log.info(format("Updating relator term mapping rules, subfield \"j\" for fields: %s", StringUtils.collectionToCommaDelimitedString(contributorsFieldsWithSubfieldJtoUpdate)));
        contributorsFieldsWithSubfieldJtoUpdate.forEach(field -> {
            JsonNode fieldRule = mappingRules.get(field);
            if (fieldRule != null) {
                try {
                    removeTarget(fieldRule, CONTRIBUTOR_TYPE_ID);
                    removeTarget(fieldRule, CONTRIBUTOR_TYPE_TEXT);
                    addTarget(fieldRule, UPDATED_MAPPING_RULES_PATH + RELATOR_TERM_J_SUBFIELD_TARGET);
                } catch (Exception e) {
                    log.warn(format("Cannot update relator term mapping rules, subfield \"j\", field: %s", field), e);
                    throw e;
                }
            }
        });
    }

    public void authorityControlUpdate(JsonNode mappingRules) {
        List<String> alternativeTitlesFields = findFieldsContainsTarget(mappingRules, ALTERNATIVE_TITLES);
        log.info(format(UPDATING_AUTHORITY_CONTROL_LOG, ALTERNATIVE_TITLES, StringUtils.collectionToCommaDelimitedString(alternativeTitlesFields)));
        alternativeTitlesFields.forEach(field -> {
            JsonNode fieldRule = mappingRules.get(field);
            if (fieldRule != null) {
                try {
                    addTarget(fieldRule, UPDATED_MAPPING_RULES_PATH + ALTERNATIVE_TITLE_TARGET);
                } catch (Exception e) {
                    log.warn(format(CANNOT_UPDATE_AUTHORITY_CONTROL_LOG, ALTERNATIVE_TITLES, field), e);
                    throw e;
                }
            }
        });

        List<String> contributorsFields = findFieldsContainsTarget(mappingRules, CONTRIBUTORS);
        log.info(format(UPDATING_AUTHORITY_CONTROL_LOG, CONTRIBUTORS, StringUtils.collectionToCommaDelimitedString(contributorsFields)));
        contributorsFields.forEach(field -> {
            JsonNode fieldRule = mappingRules.get(field);
            if (fieldRule != null) {
                try {
                    addTarget(mappingRules.get(field), UPDATED_MAPPING_RULES_PATH + CONTRIBUTORS_TARGET);
                } catch (Exception e) {
                    log.warn(format(CANNOT_UPDATE_AUTHORITY_CONTROL_LOG, CONTRIBUTORS, field), e);
                    throw e;
                }
            }
        });

        List<String> seriesFields = findFieldsContainsTarget(mappingRules, SERIES);
        log.info(format(UPDATING_AUTHORITY_CONTROL_LOG, SERIES, StringUtils.collectionToCommaDelimitedString(seriesFields)));
        seriesFields.forEach(field -> {
            JsonNode fieldRule = mappingRules.get(field);
            if (fieldRule != null) {
                try {
                    JsonNode fieldRules = mappingRules.get(field);
                    surroundRulesWithEntity(fieldRules);
                    addValueToTarget(fieldRules, SERIES);
                    addTarget(fieldRules, UPDATED_MAPPING_RULES_PATH + SERIES_TARGET);
                } catch (Exception e) {
                    log.warn(format(CANNOT_UPDATE_AUTHORITY_CONTROL_LOG, SERIES, field), e);
                    throw e;
                }
            }
        });

        List<String> subjectFields = findFieldsContainsTarget(mappingRules, SUBJECTS);
        log.info(format(UPDATING_AUTHORITY_CONTROL_LOG, SUBJECTS, StringUtils.collectionToCommaDelimitedString(subjectFields)));
        subjectFields.forEach(field -> {
            JsonNode fieldRule = mappingRules.get(field);
            if (fieldRule != null) {
                try {
                    JsonNode fieldRules = mappingRules.get(field);
                    surroundRulesWithEntity(fieldRules);
                    addValueToTarget(fieldRules, SUBJECTS);
                    addTarget(fieldRules, UPDATED_MAPPING_RULES_PATH + SUBJECTS_TARGET);
                } catch (Exception e) {
                    log.warn(format(CANNOT_UPDATE_AUTHORITY_CONTROL_LOG, SUBJECTS, field), e);
                    throw e;
                }
            }
        });
    }

    private List<String> findFieldsContainsTarget(JsonNode mappingRules, String targetName) {
        List<String> fields = new LinkedList<>();
        Iterator<Map.Entry<String, JsonNode>> fieldsIterator = mappingRules.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> field = fieldsIterator.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            if (fieldContainsTarget(fieldValue, targetName)) fields.add(fieldName);
        }
        return fields;
    }

    private boolean fieldContainsTarget(JsonNode fieldValue, String targetName) {
        List<String> targetValue = fieldValue.findValuesAsText(TARGET);
        return targetValue.stream().anyMatch(target -> target.equals(targetName) || target.contains(targetName + DOT));
    }

    private void removeTarget(JsonNode fieldRules, String targetName) {
        for (JsonNode entityRule : fieldRules) {
            ArrayNode rules = (ArrayNode) entityRule.get(ENTITY);
            for (int i = 0; i < rules.size(); i++) {
                JsonNode rule = rules.get(i);
                String targetValue = rule.get(TARGET).asText();
                if (targetValue.equals(targetName)) {
                    rules.remove(i);
                }
            }
        }
    }

    private void addTarget(JsonNode fieldRules, String targetPath) {
        JsonNode newTarget = FileWorker.getJsonObject(targetPath);
        for (JsonNode entityRule : fieldRules) {
            ArrayNode rules = (ArrayNode) entityRule.get(ENTITY);
            if (!isContainsTarget(rules, newTarget.get(TARGET).asText())) {
                rules.add(newTarget);
            }
        }
    }

    private void surroundRulesWithEntity(JsonNode fieldRules) {
        if (fieldRules.findValue(ENTITY) == null) {
            ArrayNode arrayNode = new ArrayNode(JsonNodeFactory.instance);
            for (JsonNode fieldRule : fieldRules) {
                arrayNode.add(fieldRule);
            }
            ((ArrayNode) fieldRules).removeAll();
            ObjectNode ruleSurroundedWithEntity = new ObjectNode(JsonNodeFactory.instance);
            ruleSurroundedWithEntity.putIfAbsent(ENTITY, arrayNode);
            ((ArrayNode) fieldRules).add(ruleSurroundedWithEntity);
        }
    }

    private void addValueToTarget(JsonNode fieldRules, String targetName) {
        for (JsonNode fieldRule : fieldRules) {
            for (JsonNode rule : fieldRule.get(ENTITY)) {
                if (rule.get(TARGET).asText().equals(targetName)) {
                    ((ObjectNode) rule).put(TARGET, targetName + DOT + VALUE);
                }
            }
        }
    }

    private boolean isContainsTarget(ArrayNode rules, String targetName) {
        for (JsonNode rule : rules) {
            if (rule.get(TARGET).asText().equals(targetName)) {
                return true;
            }
        }
        return false;
    }
}
