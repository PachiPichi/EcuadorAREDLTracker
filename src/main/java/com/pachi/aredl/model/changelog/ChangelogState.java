package com.pachi.aredl.model.changelog;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
public class ChangelogState {

    @JsonProperty("known_record_ids")
    private Set<String> knownRecordIds = new LinkedHashSet<>();

    @JsonProperty("known_player_ids")
    private Set<String> knownPlayerIds = new LinkedHashSet<>();

    @JsonProperty("known_records")
    private List<ChangelogRecordState> knownRecords = new ArrayList<>();

}
