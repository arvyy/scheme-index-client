package com.github.arvyy.scmindexclient.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FuncSignature {

    @Data
    public static class FuncParam {
        String name;
        List<String> types;
    }

    @Data
    public static class FuncReturn {
        String kind;
        List<FuncReturn> items;
        String type;
    }

    List<FuncParam> params;
    @JsonProperty("return")
    FuncReturn funcReturn;

}
