package com.github.arvyy.scmindexclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.http.HttpClient;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "INTEGRATION_TESTS", matches = "true")
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

}
