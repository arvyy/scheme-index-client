package com.github.arvyy.scmindexclient.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SyntaxSubsyntaxSignature {

    String name;
    List<String> patterns;

}
