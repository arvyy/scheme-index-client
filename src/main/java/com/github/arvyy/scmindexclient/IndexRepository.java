package com.github.arvyy.scmindexclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.arvyy.scmindexclient.result.SearchResult;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class IndexRepository {

    private final HttpClient client;
    private final String url;
    private String filterset;

    public IndexRepository(HttpClient client, String url) {
        this.client = client;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setSelectedFilterset(String filterset) {
        this.filterset = filterset;
    }

    public SearchResult loadIndexEntries(List<String> libraries, List<String> params, List<String> returns, List<String> tags, String query, int offset, int pagesize) throws IOException, InterruptedException {
        var finalUrl = url + "/rest/filterset/" + filterset + "/search?rows=" + pagesize + "&start=" + offset + "&facet=false&";
        for (var lib: libraries)
            finalUrl += "lib=" + URLEncoder.encode(lib, StandardCharsets.UTF_8) + "&";
        for (var param: params)
            finalUrl += "param=" + URLEncoder.encode(param, StandardCharsets.UTF_8) + "&";
        for (var ret: returns)
            finalUrl += "return=" + URLEncoder.encode(ret, StandardCharsets.UTF_8) + "&";
        for (var tag: tags)
            finalUrl += "tag=" + URLEncoder.encode(tag, StandardCharsets.UTF_8) + "&";
        finalUrl += "query=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
        var req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(finalUrl))
                .build();
        var resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
        return new ObjectMapper().readValue(resp.body(), SearchResult.class);
    }

    public List<String> loadFiltersets() {
        try {
            var req = HttpRequest.newBuilder()
                    .GET()
                    .uri(URI.create(url + "/rest/filterset"))
                    .build();
            var resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            return new ObjectMapper().readValue(resp.body(), new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to load filtersets", e);
        }
    }

    public List<String> getTags() {
        return getFacets("tags");
    }

    public List<String> getLibraries() {
        return getFacets("libs");
    }

    public List<String> getParameters() {
        return getFacets("params");
    }

    public List<String> getReturns() {
        return getFacets("returns");
    }

    private List<String> getFacets(String facet) {
        try {
            var req = HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI(url + "/rest/filterset/" + filterset + "/" + facet))
                    .build();
            var resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            var rez = new ObjectMapper().readValue(resp.body(), new TypeReference<List<String>>() {});
            return rez;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load " + facet, e);
        }
    }
}
