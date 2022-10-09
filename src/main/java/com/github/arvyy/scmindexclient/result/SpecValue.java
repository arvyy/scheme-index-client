package com.github.arvyy.scmindexclient.result;

import lombok.Data;

import java.util.List;

@Data
public class SpecValue {

    @Data
    public static class SpecValueEntry {
        String value;
        String desc;
    }

    String field;
    List<SpecValueEntry> values;

}
