package com.wzq.rpc.utils.file;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author wzq
 * @create 2022-12-09 19:36
 */
@Slf4j
public class PropertiesFileUtils {

    private PropertiesFileUtils() {
    }

    public static Properties readPropertiesFile(String fileName) {
        String rpcConfigPath = "";
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        if (url != null) {
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;

        try (FileInputStream fileInputStream = new FileInputStream(rpcConfigPath)) {
            properties = new Properties();
            properties.load(fileInputStream);
        } catch (IOException e) {
            log.error("occur exception when read properties file [{}]", fileName);
        }

        return properties;
    }

}
