package dev.jsinco.brewery.garden.utility;

import java.util.logging.Level;

public class Logger {

    public static void log(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[Garden Info - " + className + ":" + caller.getLineNumber() + "] " + message;
        logger().log(Level.INFO, prefixedMessage);
    }

    public static void logErr(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[Garden Error - " + className + ":" + caller.getLineNumber() + "] " + message;
        logger().log(Level.SEVERE, prefixedMessage);
    }

    public static void logErr(Throwable throwable) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefix = "[Garden Error - " + className + ":" + caller.getLineNumber() + "] ";
        logger().log(Level.SEVERE, prefix + throwable.getMessage(), throwable);
    }

    public static void logWarn(String message) {
        StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        String className = caller.getClassName().substring(caller.getClassName().lastIndexOf('.') + 1);
        String prefixedMessage = "[TBP Warning - " + className + ":" + caller.getLineNumber() + "] " + message;
        logger().log(Level.WARNING, prefixedMessage);
    }

    private static java.util.logging.Logger logger() {
        return java.util.logging.Logger.getLogger("Garden");
    }
}
