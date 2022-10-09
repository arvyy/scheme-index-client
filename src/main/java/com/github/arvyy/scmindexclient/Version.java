package com.github.arvyy.scmindexclient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Version {

    private static String version;
    public static String getVersion() {
        if (version != null) {
            return version;
        }
        version = loadVersion();
        return version;
    }

    private static String loadVersion() {
        byte[] bytes = new byte[0];
        try {
            bytes = Version.class.getResourceAsStream("/version.txt").readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load version", e);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
