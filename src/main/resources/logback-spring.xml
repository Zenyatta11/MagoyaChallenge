<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <root level="info">
        <appender-ref ref="JsonConsole"/>
    </root>

    <logger name="com.zenyatta" level="info"/>

    <appender name="JsonConsole" class="ch.qos.logback.core.ConsoleAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>${LOG_LEVEL}</level>
        </filter>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <shortenedLoggerNameLength>48</shortenedLoggerNameLength>
            <timeZone>UTC</timeZone>
            <fieldNames>
                <levelValue>level</levelValue>
                <level>level_name</level>
                <timestamp>datetime</timestamp>
                <thread>[ignore]</thread>
                <version>[ignore]</version>
                <stackTrace>stacktrace</stackTrace>
            </fieldNames>
            <throwableConverter class="net.logstash.logback.stacktrace.ShortenedThrowableConverter">
                <rootCauseFirst>true</rootCauseFirst>
                <maxDepthPerThrowable>36</maxDepthPerThrowable>
                <maxLength>1024</maxLength>
                <shortenedClassNameLength>30</shortenedClassNameLength>
                <exclude>^sun\.reflect\..*\.invoke</exclude>
                <exclude>^net\.sf\.cglib\.proxy\.MethodProxy\.invoke</exclude>
            </throwableConverter>
        </encoder>
    </appender>
</configuration>
