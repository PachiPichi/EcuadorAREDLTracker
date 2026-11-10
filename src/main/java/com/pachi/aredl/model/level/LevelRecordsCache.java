package com.pachi.aredl.model.level;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.pachi.aredl.model.country.CountryRecord;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LevelRecordsCache {

    @JsonProperty("completion_count")
    private Integer completionCount;

    private List<CountryRecord> records;
}
