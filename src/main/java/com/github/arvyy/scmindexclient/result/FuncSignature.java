package com.github.arvyy.scmindexclient.result;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FuncSignature {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FuncParam {
        String name;
        List<String> types;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FuncReturn {
        String kind;
        List<FuncReturn> items;
        String type;
    }

    List<FuncParam> params;
    @JsonProperty("return")
    FuncReturn funcReturn;

}
