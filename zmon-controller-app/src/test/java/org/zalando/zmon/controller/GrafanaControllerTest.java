package org.zalando.zmon.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.zalando.zmon.domain.CheckDefinition;
import org.zalando.zmon.domain.CheckResults;
import org.zalando.zmon.persistence.GrafanaDashboardSprocService;
import org.zalando.zmon.service.ZMonService;

import java.io.IOException;
import java.util.Optional;

public class GrafanaControllerTest {

    @Test
    public void dynamicCheckDashboard() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ZMonService zMonService = Mockito.mock(ZMonService.class);
        CheckResults checkResults = new CheckResults("myentity");
        CheckDefinition checkDef = new CheckDefinition();
        checkDef.setId(123);
        Mockito.when(zMonService.getCheckDefinitionById(123)).thenReturn(Optional.of(checkDef));
        Mockito.when(zMonService.getCheckResults(123, null, 1)).thenReturn(Lists.newArrayList(checkResults));

        GrafanaDashboardSprocService grafanaSprocService = Mockito.mock(GrafanaDashboardSprocService.class);
        GrafanaController controller = new GrafanaController(zMonService, grafanaSprocService, null, mapper, null);
        ResponseEntity<JsonNode> response = controller.serveDynamicDashboard("zmon-check-123");
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().get("dashboard").get("links").get(0).get("url").textValue()).isEqualTo("/#/check-definitions/view/123");

    }
}
