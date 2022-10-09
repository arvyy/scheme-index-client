package com.github.arvyy.scmindexclient.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyntaxSignature {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SyntaxPattern {
        String pattern;
        String type;
    }

    List<String> literals;
    List<SyntaxPattern> patterns;

}
