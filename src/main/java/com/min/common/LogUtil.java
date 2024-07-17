package com.min.common;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogUtil {
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static void log(Object... args) {

        String now = LocalTime.now().format(formatter);
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        sb.append(Thread.currentThread().getName());
        sb.append("]");
        sb.append("[");
        sb.append(now);
        sb.append("]: ");
        for (Object arg : args) {
            sb.append(arg);
            sb.append("\t");
        }
        FileUtil.append("logs/log.log", sb.toString());
        System.out.println(sb);
    }
}