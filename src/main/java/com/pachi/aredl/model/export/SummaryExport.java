package com.pachi.aredl.model.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SummaryExport {

    private final int rank;
    private final int extremesRank;

    private final int levelPoints;
    private final int extremes;

    private final int totalLevels;
    private final long completedLevels;
    private final long pendingLevels;
}
