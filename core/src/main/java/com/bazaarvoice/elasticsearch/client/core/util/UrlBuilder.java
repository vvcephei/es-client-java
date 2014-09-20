package com.bazaarvoice.elasticsearch.client.core.util;

import org.elasticsearch.common.base.Joiner;
import org.elasticsearch.common.base.Optional;
import org.elasticsearch.common.base.Splitter;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.collect.ImmutableSet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.elasticsearch.common.Preconditions.checkNotNull;

public class UrlBuilder {
    private final String protocol;
    private final String host;
    private final Integer port;
    private final String path;
    private final String query;

    public static UrlBuilder create() {
        return new UrlBuilder(null, null, null, null, null);
    }

    private UrlBuilder(String protocol, String host, Integer port, String path, String query) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
        this.query = query;
    }

    public UrlBuilder protocol(final String protocol) {
        validateAlphaOr(protocol);
        return new UrlBuilder(protocol, host, port, path, query);
    }

    public UrlBuilder host(final String host) {
        validateAlphaOr(host);
        return new UrlBuilder(protocol, host, port, path, query);
    }

    public UrlBuilder port(final int port) {
        return new UrlBuilder(protocol, host, port, path, query);
    }

    public UrlBuilder path(final String path) {
        List<String> split = ImmutableList.copyOf(Splitter.on('/').omitEmptyStrings().trimResults().split(path));
        for (String seg : split) {
            validateAlphaOr(seg);
        }
        String finalPath = Joiner.on('/').join(split);
        return new UrlBuilder(protocol, host, port, finalPath, query);
    }

    public UrlBuilder seg(final String... segments) {
        String partialPath = Joiner.on('/').skipNulls().join(segments);
        List<String> split = ImmutableList.copyOf(Splitter.on('/').omitEmptyStrings().trimResults().split(partialPath));
        for (String seg : split) {
            validateAlphaOr(seg, ',', '_');
        }
        String finalPartialPath = Joiner.on('/').join(split);

        String originalPath = path == null ? "" : path;
        return new UrlBuilder(protocol, host, port, originalPath + "/" + finalPartialPath, query);
    }

    /**
     * Uses Object.toString. You should prefer to call this method with a string
     *
     * @param key
     * @param value
     * @return
     */
    public UrlBuilder paramIfPresent(String key, Optional<String> value) {
        if (value.isPresent()) {
            validateAlphaOr(key);
            validateAlphaOr(value.get());
            String newParam = key + "=" + value;
            String newQuery = query == null ? newParam : query + "&" + newParam;
            return new UrlBuilder(protocol, host, port, path, newQuery);
        } else {
            return this;
        }
    }

    public URL url() {
        checkNotNull(protocol);
        checkNotNull(host);
        checkNotNull(port);
        checkNotNull(path);
        try {
            return new URL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }


    private static void validateAlphaOr(String string, Character... allowed) {
        checkNotNull(string);
        ImmutableSet<Character> allowedChars = ImmutableSet.copyOf(allowed);
        for (char c : string.toCharArray()) {
            if (!Character.isAlphabetic(c) && !allowedChars.contains(c)) {
                throw new IllegalArgumentException(String.format("%s is not allowed in %s", c, string));
            }
        }
    }
}
