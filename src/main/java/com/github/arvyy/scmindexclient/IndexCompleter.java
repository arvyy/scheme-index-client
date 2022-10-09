package com.github.arvyy.scmindexclient;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

class IndexCompleter implements Completer {

    private IndexRepository indexRepository;

    public void setRepository(IndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }

    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        String buffer = parsedLine.word();
        if (buffer.startsWith("t:")) {
            list.addAll(suggestFacetCompletion(buffer, "t:", indexRepository::getTags));
        }
        if (buffer.startsWith("p:")) {
            list.addAll(suggestFacetCompletion(buffer, "p:", indexRepository::getParameters));
        }
        if (buffer.startsWith("r:")) {
            list.addAll(suggestFacetCompletion(buffer, "r:", indexRepository::getReturns));
        }
        if (buffer.startsWith("l:")) {
            list.addAll(suggestFacetCompletion(buffer, "l:", indexRepository::getLibraries));
        }
    }

    List<Candidate> suggestFacetCompletion(String buffer, String prefix, Supplier<List<String>> loader) {
        var tagStart = buffer.substring(prefix.length());
        return loader.get()
                .stream()
                .filter(t -> t.startsWith(tagStart))
                .map(e -> new Candidate(prefix + e))
                .collect(Collectors.toList());
    }

}
