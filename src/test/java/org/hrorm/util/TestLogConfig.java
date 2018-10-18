package org.hrorm.util;

public class TestLogConfig {
    public static void load(){
        System.setProperty("java.util.logging.config.file", ClassLoader.getSystemResource("logging.properties").getPath());
    }
}
