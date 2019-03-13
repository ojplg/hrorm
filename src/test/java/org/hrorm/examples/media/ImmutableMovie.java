package org.hrorm.examples.media;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImmutableMovie {
    private final Long id;
    private final String title;
}
