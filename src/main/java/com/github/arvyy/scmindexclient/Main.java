package com.github.arvyy.scmindexclient;

import com.github.arvyy.scmindexclient.result.FuncSignature;
import com.github.arvyy.scmindexclient.result.SearchItem;
import com.github.arvyy.scmindexclient.result.SearchResult;
import com.github.arvyy.scmindexclient.result.SpecValue;
import org.apache.commons.cli.*;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.*;
import java.net.http.HttpClient;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

public class Main {

    public static final String horSeparator = "------------------------------------------------------";

    public static void main(String[] args) throws Exception {
        main(args, System.out, System.err, System::exit, url -> new IndexRepository(HttpClient.newHttpClient(), url));
    }

    public static void main(String[] args, PrintStream out, PrintStream err, IntConsumer exit, Function<String, IndexRepository> repositoryFactory) throws Exception {
        var opts = new Options();
        opts.addOption(Option.builder()
                .option("u")
                .longOpt("url")
                .hasArg(true)
                .desc("Server url to make requests to. Defaults to https://index.scheme.org")
                .build());
        opts.addOption(Option.builder()
                .option("l")
                .longOpt("logproperties")
                .hasArg(true)
                .desc("Reference to logging configuration file. See Java Util Logging documentation for details.")
                .build());
        opts.addOption(Option.builder()
                .option("f")
                .longOpt("filterset")
                .hasArg(true)
                .desc("Filterset (scheme implementation) to use (required for non-interactive mode)")
                .build());
        opts.addOption(Option.builder()
                .option("q")
                .longOpt("query")
                .hasArg(true)
                .desc("Search query (required for non-interactive mode)")
                .build());
        opts.addOption(Option.builder()
                .option("r")
                .longOpt("rows")
                .hasArg(true)
                .desc("Row count / page size, defaults to 10")
                .build());
        opts.addOption(Option.builder()
                .option("p")
                .longOpt("page")
                .hasArg(true)
                .desc("Page index, 0-based (non-interactive mode)")
                .build());
        opts.addOption(Option.builder()
                .option("h")
                .longOpt("help")
                .hasArg(false)
                .desc("Show help")
                .build());
        opts.addOption(Option.builder()
                .option("s")
                .longOpt("strict")
                .hasArg(false)
                .desc("Use strict / exact search by name (non-interactive mode)")
                .build());

        var cliParser = new DefaultParser();
        CommandLine cl;
        try {
            cl = cliParser.parse(opts, args);
        } catch (Exception e) {
            err.println(e.getMessage());
            exit.accept(1);
            return;
        }

        if (cl.hasOption("h")) {
            printHelp(out, opts);
            return;
        }

        if (cl.hasOption("l")) {
            try {
                var f = new File(cl.getOptionValue("l"));
                if (f.exists())
                    LogManager.getLogManager().readConfiguration(new FileInputStream(f));
            } catch (IOException e) {
                err.println("Failed to read logging configuration: " + e.getMessage());
                e.printStackTrace(err);
                exit.accept(1);
                return;
            }
        }

        String url = cl.getOptionValue("u", "https://index.scheme.org");
        String query = cl.getOptionValue("q");
        String filterset = cl.getOptionValue("f");
        boolean strict = cl.hasOption("s");
        int page, pageSize;
        try {
            var pageStr = cl.getOptionValue("p", "0");
            page = Integer.parseInt(pageStr);
        } catch (Exception e) {
            err.println("Given page is not a positive integer");
            exit.accept(1);
            return;
        }
        try {
            var pageSizeStr = cl.getOptionValue("r", "10");
            pageSize = Integer.parseInt(pageSizeStr);
        } catch (Exception e) {
            err.println("Given page size is not a positive integer");
            exit.accept(1);
            return;
        }

        var repo = repositoryFactory.apply(url);
        if (query != null && filterset != null) {
            runSingleQuery(out, repo, filterset, query, strict, pageSize, page);
        } else {
            launchRepl(exit, repo, filterset, pageSize);
        }
    }

    private static void runSingleQuery(PrintStream out, IndexRepository repo, String filterset, String query, boolean strict, int pageSize, int page) throws IOException, InterruptedException {
        repo.setSelectedFilterset(filterset);
        var parsedQuery = parseQuery(query);
        var q = parsedQuery.getQuery();
        if (strict) {
            q = "name_precise:\"" + q + "\"";
        }
        var result = repo.loadIndexEntries(List.of(), parsedQuery.getParams(), parsedQuery.getReturns(), List.of(), q, page * pageSize, pageSize);
        for (var l: renderResponse(result, page, pageSize)) {
            out.println(l);
        }
    }

    static void printHelp(PrintStream out, Options opts) {
        var formatter = new HelpFormatter();
        var footer =
                "Scheme Index rest client\nReport issues at https://github.com/arvyy/scheme-index-client\nVersion " + Version.getVersion() + "\n\n" +
                "Client has 2 distinct modes -- interactive and non-interactive.\n" +
                "Non-interactive mode accepts a query input through the argument, executes it, prints the result, and exits. It is " +
                "intended to be used programmatically from other software (for example, from VIM). " +
                "To start non-interactive mode, both q/query and f/filterset parameters must be provided.\n" +
                "Interactive mode starts a REPL mode, accepting and printing queries until SIGINT is received.  \n\n" +
                "Query is parsed as following. First, the line is tokenized by space. Each word that starts with `p:` is interpreted as a parameter type filter. " +
                "Each word that starts with `r:` is interpreted as a return type filter. Rest of the words are joined by space and passed as a query. For example, " +
                "`p:list? ref` would find functions that take a list as a parameter and have 'ref' in their name, such as the function list-ref. If running in interactive " +
                "mode, after entering p: or r:, the rest may be autocompleted with tab key. In interactive mode upon receiving paginated result, " +
                "press enter with empty input to display the next page.";
        PrintWriter pw = new PrintWriter(out);
        formatter.printHelp(pw, formatter.getWidth(),"scmindex", "", opts, formatter.getLeftPadding(), formatter.getDescPadding(), footer, true);
        pw.flush();
    }

    static void launchRepl(IntConsumer exit, IndexRepository repo, String filterset, int pageSize) throws Exception {
        var terminal = TerminalBuilder
                .builder()
                .jna(false)
                .nativeSignals(true)
                .nativeSignals(true)
                .signalHandler(Terminal.SignalHandler.SIG_DFL)
                .build();
        var completer = new IndexCompleter();
        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(completer)
                .build();

        terminal.writer().println("Using server: " + repo.getUrl());
        terminal.writer().println("Run with `-h` for help");
        terminal.writer().println();

        if (filterset == null)
            filterset = selectFilterset(terminal.writer(), reader, repo);
        repo.setSelectedFilterset(filterset);
        completer.setRepository(repo);

        String next = null;
        while (true) {
            try {
                String line = next == null? reader.readLine(filterset + ">") : next;
                next = null;
                if (line.trim().isEmpty())
                    continue;
                next = handleInput(terminal.writer(), reader, repo, line, pageSize);
            } catch (UserInterruptException e) {
                exit.accept(0);
                return;
            }
        }
    }

    static String selectFilterset(PrintWriter writer, LineReader reader, IndexRepository indexRepository) throws Exception {
        writer.println("Select filterset");
        var filtersets = indexRepository.loadFiltersets();
        for (var i = 0; i < filtersets.size(); i++) {
            writer.println((i + 1) + ". " + filtersets.get(i));
        }

        while (true) {
            int choice;
            try {
                choice = Integer.parseInt(reader.readLine(">"));
                if (choice >= 1 && choice <= filtersets.size()) {
                    return filtersets.get(choice - 1);
                }
            } catch (NumberFormatException e) {
            } catch (UserInterruptException e) {
                System.exit(0);
            }
        }
    }

    public static ParsedQuery parseQuery(String queryLine) {
        var params = new ArrayList<String>();
        var returns = new ArrayList<String>();
        var query = new StringJoiner(" ");
        var sc = new Scanner(queryLine);
        while (sc.hasNext()) {
            var token = sc.next();
            if (token.startsWith("p:"))
                params.add(token.substring(2));
            else if (token.startsWith("r:"))
                returns.add(token.substring(2));
            else query.add(token);
        }
        var r = new ParsedQuery();
        r.setQuery(query.toString());
        r.setParams(params);
        r.setReturns(returns);
        return r;
    }

    public static String handleInput(PrintWriter writer, LineReader reader, IndexRepository repo, String line, int pageSize) throws IOException, InterruptedException {
        var page = 0;
        var parsedQuery = parseQuery(line);
        while (true) {
            var resp = repo.loadIndexEntries(List.of(), parsedQuery.getParams(), parsedQuery.getReturns(), List.of(), parsedQuery.getQuery(), page * pageSize, pageSize);
            for (var l: renderResponse(resp, page, pageSize)) {
                writer.println(l);
            }
            if ((page + 1) * pageSize > resp.getTotal()) {
                return null;
            } else {
                String nextLine = reader.readLine();
                if (nextLine.trim().isEmpty()) {
                    page++;
                } else {
                    return nextLine;
                }
            }
        }
    }

    public static List<String> renderResponse(SearchResult result, int page, int pageSize) {
        var lst = new ArrayList<String>(10 * result.getItems().size());
        for (var i: result.getItems()) {
            lst.add(horSeparator);
            lst.add(i.getName() + ": " + (i.getType() == SearchItem.SearchItemType.value? i.getReturn_types().get(0) : i.getType()));
            if (i.getType() == SearchItem.SearchItemType.function)
                lst.addAll(renderFunction(i));
            if (i.getType() == SearchItem.SearchItemType.syntax)
                lst.addAll(renderSyntax(i));
            if (!i.getTags().isEmpty()) {
                lst.add(i.getTags().stream().map(t -> "[" + t + "]").collect(Collectors.joining(" ")));
            }
            lst.addAll(renderSpecValues(i.getSpec_values()));
            lst.add("library " + i.getLib());
        }
        lst.add(horSeparator);
        lst.add("Page " + (page + 1) + " / " + (int) Math.ceil((double) result.getTotal() / pageSize));
        return lst;
    }

    public static List<String> renderSpecValues(List<SpecValue> spec_values) {
        var lst = new ArrayList<String>();
        for (var s: spec_values) {
            lst.add(s.getField() + ": ");
            for (var v: s.getValues()) {
                lst.add("    " + v.getValue() + " - " + v.getDesc());
            }
            lst.add("");
        }
        return lst;
    }

    public static List<String> renderFunction(SearchItem fn) {
        var lst = new ArrayList<String>();
        lst.addAll(renderFunctionSignature(fn.getFunc_signature()));
        if (!fn.getFunc_param_signatures().isEmpty()) {
            lst.add("");
            for (var s: fn.getFunc_param_signatures()) {
                var paramSig = renderFunctionSignature(s.getSignature());
                paramSig.set(0, s.getName() + ": " + paramSig.get(0));
                lst.addAll(paramSig);
            }
        }
        return lst;
    }

    public static List<String> renderFunctionSignature(FuncSignature signature) {
        var lst = new ArrayList<String>();
        var sj = new StringJoiner(", ", "[", "]");
        for (var p: signature.getParams()) {
            if (p.getTypes().isEmpty()) {
                sj.add(p.getName());
            } else {
                sj.add("(" + p.getTypes().stream().collect(Collectors.joining("/")) + " " + p.getName() + ")");
            }
        }
        lst.add(sj + " => " + renderFunctionReturn(signature.getFuncReturn()));
        return lst;
    }

    public static String renderFunctionReturn(FuncSignature.FuncReturn ret) {
        if (ret.getKind().equals("return")) {
            return ret.getType();
        } else {
            var sb = new StringBuilder();
            sb.append("(");
            sb.append(ret.getKind());
            sb.append(" ");
            sb.append(ret.getItems().stream().map(r -> renderFunctionReturn(r)).collect(Collectors.joining(" ")));
            sb.append(")");
            return sb.toString();
        }
    }

    public static List<String> renderSyntax(SearchItem syntax) {
        var lst = new ArrayList<String>();
        if (!syntax.getSyntax_signature().getLiterals().isEmpty())
            lst.add("Literals: " + syntax.getSyntax_signature().getLiterals().stream().collect(Collectors.joining(" ")));
        for (var p: syntax.getSyntax_signature().getPatterns()) {
            var s = p.getPattern();
            if (p.getType() != null)
                s += " => " + p.getType();
            lst.add(s);
        }
        if (!syntax.getSyntax_subsyntax_signatures().isEmpty()) {
            for (var s: syntax.getSyntax_subsyntax_signatures()) {
                lst.add(s.getName() + " :=");
                for (var p: s.getPatterns()) {
                    lst.add("    " + p);
                }
            }
        }
        if (!syntax.getSyntax_param_signatures().isEmpty()) {
            for (var s: syntax.getSyntax_param_signatures()) {
                lst.add(s.getName() + ": " + s.getType());
            }
        }
        return lst;
    }
}