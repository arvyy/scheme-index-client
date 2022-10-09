package com.github.arvyy.scmindexclient.result;

import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    List<FacetValue> libs;
    List<FacetValue> params;
    List<FacetValue> returns;
    List<FacetValue> tags;
    List<FacetValue> parameterized;
    Integer total;
    List<SearchItem> items;

}
