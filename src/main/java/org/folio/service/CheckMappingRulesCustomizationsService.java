package org.folio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.folio.client.AuthClient;
import org.folio.client.SRMClient;
import org.folio.model.Configuration;
import org.folio.util.FileWorker;
import org.folio.util.HttpWorker;
import org.folio.util.MappingRulesUtil;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.folio.FolioMappingRulesCheckCustomizationsApp.exitWithMessage;

@Slf4j
@Service
public class CheckMappingRulesCustomizationsService {
    private static final String ORCHID_MAPPING_RULES_PATH =
        "mappingRules/orchidMappingRules/classificationsAndContributorsMappingRules.json";
    private String MARC_BIB_RECORD_TYPE = "marc-bib";

    private Configuration configuration;
    private SRMClient srmClient;


    public void start() {
        configuration = FileWorker.getConfiguration();
        var httpWorker = new HttpWorker(configuration);
        var authClient = new AuthClient(configuration, httpWorker);

        httpWorker.setOkapiToken(authClient.authorize());
        srmClient = new SRMClient(httpWorker);

        checkMappingRulesCustomizations();

        exitWithMessage("Script execution completed");
    }

    private void checkMappingRulesCustomizations() {
        JsonNode existingMappingRules = srmClient.retrieveMappingRules(MARC_BIB_RECORD_TYPE);

        ObjectNode orchidClassificationsAndContributorsRules = FileWorker.getJsonObject(ORCHID_MAPPING_RULES_PATH);
        Map<String, JsonNode> changedMappingRules = MappingRulesUtil.getChangedMappingRules(orchidClassificationsAndContributorsRules, existingMappingRules);

        if (changedMappingRules.isEmpty()) {
            log.info("No customized mapping rules have been found for the 'classifications' and 'contributors' fields while comparing to Orchid-based mapping rules");
            return;
        }
        log.info("Customized mapping rules have been found for the following MARC fields: {}", changedMappingRules.keySet());
    }

}
