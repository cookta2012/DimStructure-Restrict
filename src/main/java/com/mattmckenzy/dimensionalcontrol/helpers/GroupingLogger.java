package com.mattmckenzy.dimensionalcontrol.helpers;

import com.mojang.logging.LogUtils;
import io.netty.handler.logging.LogLevel;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GroupingLogger
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public final static int LOG_MESSAGE_GROUPING_DELAY_SECONDS = 5;
    
    private static String lastMessage = "";
    private static LogLevel lastMessageLogLevel = LogLevel.TRACE;
    private static int lastMessageDuplicateCount = 1;
    private static LocalDateTime lastMessageDateTime = LocalDateTime.now();

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    Runnable checkPostLog = () ->
    {
        if (lastMessageDateTime.plusSeconds(LOG_MESSAGE_GROUPING_DELAY_SECONDS).isBefore(LocalDateTime.now()))
        {
            postLastLog();
        }
    };

    public GroupingLogger()
    {
        executor.scheduleAtFixedRate(checkPostLog, 0, 1, TimeUnit.SECONDS);
    }

    @SuppressWarnings("unused")
    public static void logError(String logMessage)
    {
        log(String.format("%s", logMessage), LogLevel.ERROR);
    }

    public static void logWarning(String logMessage)
    {
        log(String.format("%s", logMessage), LogLevel.WARN);
    }

    public static void logInfo(String logMessage)
    {
        log(String.format("%s", logMessage), LogLevel.INFO);
    }

    public static void logDebug(String logMessage)
    {
        log(String.format("%s", logMessage), LogLevel.DEBUG);
    }

    @SuppressWarnings("unused")
    public static void logTrace(String logMessage)
    {
        log(String.format("%s", logMessage), LogLevel.TRACE);
    }

    private static void log(String logMessage, LogLevel logLevel)
    {
        if (!Objects.equals(lastMessage, "") && Objects.equals(lastMessage, logMessage))
        {
            lastMessageDuplicateCount++;
        }
        else
        {
            postLastLog();
            lastMessage = logMessage;
            lastMessageLogLevel = logLevel;
            lastMessageDuplicateCount = 1;
        }

        lastMessageDateTime = LocalDateTime.now();
    }

    private static void postLastLog()
    {
        String duplicatedMessage = lastMessageDuplicateCount > 1 ? String.format("(%sx) ", lastMessageDuplicateCount) : "";

        switch (lastMessageLogLevel)
        {
            case ERROR -> LOGGER.error("{}{}", duplicatedMessage , lastMessage);
            case WARN -> LOGGER.warn("{}{}", duplicatedMessage, lastMessage);
            case INFO -> LOGGER.info("{}{}", duplicatedMessage, lastMessage);
            case DEBUG -> LOGGER.debug("{}{}", duplicatedMessage, lastMessage);
            case TRACE -> LOGGER.trace("{}{}", duplicatedMessage, lastMessage);
        }

        lastMessage = "";
        lastMessageDuplicateCount = 1;
        lastMessageLogLevel = LogLevel.TRACE;
    }
}
