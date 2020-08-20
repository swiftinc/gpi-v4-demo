package com.swift.developers.sandbox.util;

import java.util.HashMap;
import java.util.Map;

public class ConfigMap {


    private static ConfigMap instance;
    private Map<String, String> configMap;

    private ConfigMap() {
        configMap = new HashMap<String, String>();
    }

    public static ConfigMap getInstance() {
        if (instance == null) {
            instance = new ConfigMap();
        }
        return instance;
    }

    public String getConfigValue(String key) {
        String value = null;
        if (null != configMap) {
            if (null != configMap.get(key)) {
                value = configMap.get(key);
            }
        }
        return value;
    }

    public void setConfigValue(String key, String value) {
        if (null != configMap) {
            configMap.put(key, value);
        }
    }
}
