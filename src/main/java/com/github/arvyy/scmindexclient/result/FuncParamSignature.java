package com.github.arvyy.scmindexclient.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FuncParamSignature {

    String name;
    FuncSignature signature;

}
