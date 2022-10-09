package com.github.arvyy.scmindexclient.result;

import lombok.Data;

import java.util.List;

@Data
public class SyntaxSubsyntaxSignature {

    String name;
    List<String> patterns;

}
