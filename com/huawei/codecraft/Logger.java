package com.huawei.codecraft;

import com.huawei.codecraft.Config;

import java.io.FileWriter;
import java.io.IOException;

public class Logger {

    public static void error(String title, String message) {
//        if (Config.DEBUG_LEVEL < 0) return;
        String content = title + " " + message;
        write("error.log", content);
    }

    public static void info(String title, String message) {
        if (Config.DEBUG_LEVEL < 1) return;
        String content = title + " " + message;
        write("info.log", content);
    }

    public static void debug(String title, String message) {
        if (Config.DEBUG_LEVEL < 2) return;
        String content = title + " " + message;
        write("info.log", content);
    }

    private static void write(String filepath, String content) {
        try (FileWriter output = new FileWriter(filepath, true)) {
            output.append(content + '\n');
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
