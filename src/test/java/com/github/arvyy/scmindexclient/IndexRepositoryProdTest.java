package com.github.arvyy.scmindexclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named = "integrationtest", matches = "true")
public class IndexRepositoryProdTest {

    @Test
    public void testTags() {
        var r = new IndexRepository(HttpClient.newHttpClient(), "https://index.scheme.org");
        var filterset = r.loadFiltersets().get(0);
        r.setSelectedFilterset(filterset);
        var lst = r.getTags();
        assertTrue(lst.size() > 0);
    }

    @Test
    public void testParams() {
        var r = new IndexRepository(HttpClient.newHttpClient(), "https://index.scheme.org");
        var filterset = r.loadFiltersets().get(0);
        r.setSelectedFilterset(filterset);
        var lst = r.getParameters();
        assertTrue(lst.size() > 0);
    }

    @Test
    public void testReturns() {
        var r = new IndexRepository(HttpClient.newHttpClient(), "https://index.scheme.org");
        var filterset = r.loadFiltersets().get(0);
        r.setSelectedFilterset(filterset);
        var lst = r.getReturns();
        assertTrue(lst.size() > 0);
    }

    @Test
    public void testLibs() {
        var r = new IndexRepository(HttpClient.newHttpClient(), "https://index.scheme.org");
        var filterset = r.loadFiltersets().get(0);
        r.setSelectedFilterset(filterset);
        var lst = r.getLibraries();
        assertTrue(lst.size() > 0);
    }

    @Test
    public void testSearch() throws IOException, InterruptedException {
        var r = new IndexRepository(HttpClient.newHttpClient(), "https://index.scheme.org");
        var filterset = r.loadFiltersets().get(0);
        r.setSelectedFilterset(filterset);
        var result = r.loadIndexEntries(List.of(), List.of(), List.of(), List.of(), "car", 0, 1);
        assertTrue(result.getTotal() > 0);
        assertNotNull(result.getItems().get(0));
        Main.renderResponse(result, 0, 100);
    }

}
