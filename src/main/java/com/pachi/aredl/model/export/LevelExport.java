package com.pachi.aredl.model.export;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LevelExport {

    private final String id;
    private final String name;
    private final Integer position;

    @JsonProperty("nlw_tier")
    private final String nlwTier;

    @JsonProperty("dlec_position")
    private final Integer dlecPosition;

    private final boolean completed;
    private final List<VictorExport> victors;
}
