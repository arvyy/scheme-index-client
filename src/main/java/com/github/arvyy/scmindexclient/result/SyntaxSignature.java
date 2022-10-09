package com.github.arvyy.scmindexclient.result;

import lombok.Data;

import java.util.List;

@Data
public class SyntaxSignature {

    @Data
    public static class SyntaxPattern {
        String pattern;
        String type;
    }

    List<String> literals;
    List<SyntaxPattern> patterns;

}
