package org.folio.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.folio.util.HttpWorker;

import static java.lang.String.format;
import static org.folio.mapper.ResponseMapper.mapResponseToJson;

@Slf4j
public class SRMClient {
    private static final String MAPPING_RULES = "/mapping-rules/%s";
    private final HttpWorker httpWorker;

    public SRMClient(HttpWorker httpWorker) {
        this.httpWorker = httpWorker;
    }

    public JsonNode retrieveMappingRules(String recordType) {
        log.info("Retrieving mapping rules...");

        var request = httpWorker.constructGETRequest(format(MAPPING_RULES, recordType));
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed to get marc rules");

        return mapResponseToJson(response);
    }

    public void updateMappingRules(JsonNode mappingRules, String recordType) {
        log.info("Sending request to update mapping rules...");

        var request = httpWorker.constructPUTRequest(format(MAPPING_RULES, recordType), mappingRules.toString());
        var response = httpWorker.sendRequest(request);

        httpWorker.verifyStatus(response, 200, "Failed update mapping rules");
    }
}
