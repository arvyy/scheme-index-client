package com.github.arvyy.scmindexclient;

import com.github.arvyy.scmindexclient.result.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class IndexRenderingTest {

    int exitStatus;
    IntConsumer exit;
    PrintStream out;
    PrintStream err;
    ByteArrayOutputStream baosOut;
    ByteArrayOutputStream baosErr;
    IndexRepository repo;
    Function<String, IndexRepository> repoCreator;

    @BeforeEach
    public void before() {
        exitStatus = 0;
        exit = (code) -> exitStatus = code;
        baosOut = new ByteArrayOutputStream();
        baosErr = new ByteArrayOutputStream();
        out = new PrintStream(baosOut);
        err = new PrintStream(baosErr);
        repo = mock(IndexRepository.class);
        repoCreator = url -> repo;
    }

    @Test
    public void testFunctionRender() {
        var item = new SearchItem();
        item.setTags(List.of("tag"));
        item.setName("test");
        item.setLib("(scheme test)");
        item.setType(SearchItem.SearchItemType.function);
        var ret = new FuncSignature.FuncReturn(
                "values",
                List.of(new FuncSignature.FuncReturn(
                        "return",
                        List.of(),
                        "integer?"
                ), new FuncSignature.FuncReturn(
                        "return",
                        List.of(),
                        "..."
                )),
                null);
        var p1 = new FuncSignature.FuncParam("p1", List.of());
        var p2 = new FuncSignature.FuncParam("p2", List.of("procedure?"));
        var p3 = new FuncSignature.FuncParam("p3", List.of("type1", "type2"));
        item.setFunc_signature(new FuncSignature(List.of(p1, p2, p3), ret));
        var p2ParamSignature = new FuncParamSignature(
                "p2",
                new FuncSignature(
                        List.of(new FuncSignature.FuncParam("p4", List.of()),
                                new FuncSignature.FuncParam("p5", List.of("type3"))),
                        new FuncSignature.FuncReturn("return", List.of(), "undefined")));
        item.setFunc_param_signatures(List.of(p2ParamSignature));
        item.setSpec_values(List.of());
        var searchResult = new SearchResult();
        searchResult.setTotal(1);
        searchResult.setItems(List.of(item));
        var lst = Main.renderResponse(searchResult, 0, 1);
        assertEquals(List.of(
                        Main.horSeparator,
                        "test: function",
                        "[p1, (procedure? p2), (type1/type2 p3)] => (values integer? ...)",
                        "",
                        "p2: [p4, (type3 p5)] => undefined",
                        "[tag]",
                        "library (scheme test)",
                        Main.horSeparator,
                        "Page 1 / 1"),
                lst);
    }

    @Test
    public void testSyntaxRender() {
        var item = new SearchItem();
        item.setTags(List.of("tag"));
        item.setName("test");
        item.setLib("(scheme test)");
        item.setType(SearchItem.SearchItemType.syntax);
        item.setSyntax_signature(new SyntaxSignature(List.of("=>"),
                List.of(
                        new SyntaxSignature.SyntaxPattern("((foo ...) => bar)", "string?"),
                        new SyntaxSignature.SyntaxPattern("((foo ...))", null)
                )));
        item.setSyntax_subsyntax_signatures(List.of(new SyntaxSubsyntaxSignature("foo", List.of("term", "(foo ...)"))));
        item.setSyntax_param_signatures(List.of(new SyntaxParamSignature("bar", "integer?")));
        item.setSpec_values(List.of());
        var searchResult = new SearchResult();
        searchResult.setTotal(1);
        searchResult.setItems(List.of(item));
        var lst = Main.renderResponse(searchResult, 0, 1);
        assertEquals(List.of(
                Main.horSeparator,
                "test: syntax",
                "Literals: =>",
                "((foo ...) => bar) => string?",
                "((foo ...))",
                "foo :=",
                "    term",
                "    (foo ...)",
                "bar: integer?",
                "[tag]",
                "library (scheme test)",
                Main.horSeparator,
                "Page 1 / 1"
        ), lst);
    }

    @Test
    public void testValueRender() {
        var item = new SearchItem();
        item.setTags(List.of("tag"));
        item.setName("test");
        item.setLib("(scheme test)");
        item.setType(SearchItem.SearchItemType.value);
        item.setReturn_types(List.of("integer?"));
        item.setSpec_values(List.of());
        var searchResult = new SearchResult();
        searchResult.setTotal(1);
        searchResult.setItems(List.of(item));
        var lst = Main.renderResponse(searchResult, 0, 1);
        assertEquals(List.of(
                Main.horSeparator,
                "test: integer?",
                "[tag]",
                "library (scheme test)",
                Main.horSeparator,
                "Page 1 / 1"
        ), lst);
    }

    @Test
    public void testSpecvaluesRender() {
        //TODO
    }

    @Test
    public void testHelp() throws Exception {
        Main.main(new String[]{ "-h" }, out, err, exit, repoCreator);
        assertEquals(0, exitStatus);
        assertTrue(baosOut.toString(StandardCharsets.UTF_8).contains("scmindex"));
    }

    @Test
    public void testNonInteractiveQuery() throws Exception {
        SearchResult result = new SearchResult();
        SearchItem item = new SearchItem();
        item.setLib("(scheme test)");
        item.setName("test");
        item.setTags(List.of("pure"));
        item.setType(SearchItem.SearchItemType.function);
        var sig = new FuncSignature();
        var ret = new FuncSignature.FuncReturn();
        ret.setType("number?");
        ret.setKind("return");
        sig.setFuncReturn(ret);
        var param = new FuncSignature.FuncParam();
        param.setName("z");
        param.setTypes(List.of("number?"));
        sig.setParams(List.of(param));
        item.setFunc_signature(sig);
        item.setFunc_param_signatures(List.of());
        item.setSpec_values(List.of());
        result.setItems(List.of(item));
        result.setTotal(1);
        when(repo.loadIndexEntries(anyList(), anyList(), anyList(), anyList(), eq("test"), anyInt(), anyInt()))
                .thenReturn(result);
        Main.main(new String[] { "-f", "r7rs_small", "-q", "test" }, out, err, exit, repoCreator);
        assertEquals(0, exitStatus);
        var expected =
                Main.horSeparator + System.lineSeparator() +
                        "test: function" +  System.lineSeparator() +
                        "[(number? z)] => number?" + System.lineSeparator() +
                        "[pure]" + System.lineSeparator() +
                        "library (scheme test)" + System.lineSeparator() +
                        Main.horSeparator + System.lineSeparator() +
                        "Page 1 / 1" + System.lineSeparator();
        assertEquals(expected, baosOut.toString(StandardCharsets.UTF_8));
    }

}
