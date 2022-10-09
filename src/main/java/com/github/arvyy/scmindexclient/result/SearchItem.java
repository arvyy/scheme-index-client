package com.github.arvyy.scmindexclient.result;

import lombok.Data;

import java.util.List;

@Data
public class SearchItem {

    public enum SearchItemType {
        function, value, syntax;
    }

    String lib;
    String name;
    SearchItemType type;
    FuncSignature func_signature;
    List<FuncParamSignature> func_param_signatures;
    SyntaxSignature syntax_signature;
    List<SyntaxSubsyntaxSignature> syntax_subsyntax_signatures;
    List<SyntaxParamSignature> syntax_param_signatures;
    List<String> tags;
    List<String> param_types;
    List<String> return_types;
    List<String> super_types;
    List<String> parameterized_by;
    List<SpecValue> spec_values;

}
