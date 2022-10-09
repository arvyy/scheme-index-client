package com.github.arvyy.scmindexclient;

import lombok.Data;

import java.util.List;

@Data
public class ParsedQuery {

    private List<String> returns;
    private List<String> params;
    private String query;

}
