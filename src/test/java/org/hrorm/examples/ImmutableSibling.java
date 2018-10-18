package org.hrorm.examples;

@lombok.Data
@lombok.Builder
public class ImmutableSibling {
    private final Long id;
    private final String data;
}
