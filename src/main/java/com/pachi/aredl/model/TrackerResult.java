package com.pachi.aredl.model;

import com.pachi.aredl.model.country.CountryRank;
import com.pachi.aredl.model.level.TrackedLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TrackerResult {

    private final CountryRank rank;
    private final List<TrackedLevel> trackedLevels;
}
