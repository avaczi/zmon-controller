package de.zalando.zmon.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.LongNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import de.zalando.zmon.config.KairosDBProperties;
import de.zalando.zmon.config.MetricCacheProperties;
import de.zalando.zmon.domain.*;
import de.zalando.zmon.exception.ZMonException;
import de.zalando.zmon.persistence.GrafanaDashboardSprocService;
import de.zalando.zmon.rest.EntityApi;
import de.zalando.zmon.rest.domain.CheckChartResult;
import de.zalando.zmon.security.permission.DefaultZMonPermissionService;
import de.zalando.zmon.service.ZMonService;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.kairosdb.client.HttpClient;
import org.kairosdb.client.builder.*;
import org.kairosdb.client.builder.grouper.TagGrouper;
import org.kairosdb.client.response.Queries;
import org.kairosdb.client.response.QueryResponse;
import org.kairosdb.client.response.Results;
import org.kairosdb.client.response.grouping.TagGroupResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping(value = "/rest/kairosDBPost")
public class KairosDBController extends AbstractZMonController {

    @Autowired
    private KairosDBProperties kairosDBProperties;

    @ResponseBody
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    public void kairosDBPost(@RequestBody(required = true) final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        if (!kairosDBProperties.isEnabled()) {
            writer.write("");
            return;
        }

        // align all queries to full minutes
        if (node instanceof ObjectNode) {
            ObjectNode q = (ObjectNode) node;
            q.put("cache_time", 60);
            if (q.has("start_absolute")) {
                long start = q.get("start_absolute").asLong();
                start = start - (start % 60000);
                q.put("start_absolute", start);
            }
        }

        final Executor executor = Executor.newInstance();

        final String kairosDBURL = kairosDBProperties.getUrl() + "/api/v1/datapoints/query";

        final String r = executor.execute(Request.Post(kairosDBURL).useExpectContinue().bodyString(node.toString(),
                ContentType.APPLICATION_JSON)).returnContent().asString();

        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/tags", method = RequestMethod.POST, produces = "application/json")
    public void kairosDBtags(@RequestBody(required = true) final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        if (!kairosDBProperties.isEnabled()) {
            writer.write("");
            return;
        }

        final Executor executor = Executor.newInstance();

        final String kairosDBURL = kairosDBProperties.getUrl() + "/api/v1/datapoints/query/tags";

        final String r = executor.execute(Request.Post(kairosDBURL).useExpectContinue().bodyString(node.toString(),
                ContentType.APPLICATION_JSON)).returnContent().asString();

        writer.write(r);
    }

    @ResponseBody
    @RequestMapping(value = "/metrics", method = RequestMethod.GET, produces = "application/json")
    public void kairosDBmetrics(final Writer writer, final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        if (!kairosDBProperties.isEnabled()) {
            writer.write("");
            return;
        }

        final String kairosDBURL = kairosDBProperties.getUrl() + "/api/v1/metricnames";

        final String r = Request.Get(kairosDBURL).useExpectContinue().execute().returnContent().asString();

        writer.write(r);
    }

    @Autowired
    DefaultZMonPermissionService authService;

    @Autowired
    ObjectMapper mapper;


    // get list of tags
    @ResponseBody
    @RequestMapping(value = "/api/v1/datapoints/query/tags", method = RequestMethod.POST, produces = "application/json")
    public void g2kairosDBtags(@RequestBody(required = true) final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        if (!kairosDBProperties.isEnabled()) {
            writer.write("");
            return;
        }

        final Executor executor = Executor.newInstance();

        final String kairosDBURL = kairosDBProperties.getUrl() + "/api/v1/datapoints/query/tags";

        final String r = executor.execute(Request.Post(kairosDBURL).useExpectContinue().bodyString(node.toString(),
                ContentType.APPLICATION_JSON)).returnContent().asString();

        writer.write(r);
    }

    // get list of metricnames
    @ResponseBody
    @RequestMapping(value = "/api/v1/metricnames", method = RequestMethod.GET, produces = "application/json")
    public void g2kairosDBmetrics(final Writer writer, final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        if (!kairosDBProperties.isEnabled()) {
            writer.write("");
            return;
        }

        final String kairosDBURL = kairosDBProperties.getUrl() + "/api/v1/metricnames";

        final String r = Request.Get(kairosDBURL).useExpectContinue().execute().returnContent().asString();

        writer.write(r);
    }

    // get kairosdb query with datapoints
    @ResponseBody
    @RequestMapping(value = "/api/v1/datapoints/query", method = RequestMethod.POST, produces = "application/json")
    public void g2kairosDBPost(@RequestBody(required = true) final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        if (!kairosDBProperties.isEnabled()) {
            writer.write("");
            return;
        }

        final Executor executor = Executor.newInstance();

        final String kairosDBURL = kairosDBProperties.getUrl() + "/api/v1/datapoints/query";

        final String r = executor.execute(Request.Post(kairosDBURL).useExpectContinue().bodyString(node.toString(),
                ContentType.APPLICATION_JSON)).returnContent().asString();

        writer.write(r);
    }

    // save dashboard snapshot for sharing
    @ResponseBody
    @RequestMapping(value = "/api/snapshots", method = RequestMethod.POST, produces = "application/json")
    public void g2SaveSnapshots(@RequestBody(required = true) final JsonNode node, final Writer writer,
                             final HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        final String r = "{}";
        writer.write(r);
    }

}