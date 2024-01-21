package com.zenyatta.magoya.challenge.utils.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record ETag(String value) {
    private static final Pattern ETagPattern = Pattern.compile("\"([^\"]*)\"");

    @JsonCreator
    public ETag {
        final Matcher regexMatcher = ETagPattern.matcher(value);

        if (!regexMatcher.find()) {
            throw new IllegalArgumentException("Invalid ETag header");
        }
    }

    /**
     * This method formats the ETag into a <code>weak etag<code>. Weak etags prevent
     * data caching. Strong ETags will always be cached.

     * @param value
     * @return
     */
    public static ETag weak(final Object value) {
        return new ETag("W/\"%s\"".formatted(value.toString()));
    }

    public Long toLong() {
        final Matcher regexMatcher = ETagPattern.matcher(value);
        regexMatcher.find();

        return Long.parseLong(regexMatcher.group(1));
    }
}
