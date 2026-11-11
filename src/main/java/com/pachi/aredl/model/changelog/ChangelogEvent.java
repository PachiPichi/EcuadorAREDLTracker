package com.pachi.aredl.model.changelog;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogEvent {

    private String type;

    @JsonProperty("record_id")
    private String recordId;

    @JsonProperty("level_id")
    private String levelId;

    @JsonProperty("level_name")
    private String levelName;

    @JsonProperty("level_position")
    private Integer levelPosition;

    @JsonProperty("player_id")
    private String playerId;

    private String username;

    @JsonProperty("global_name")
    private String globalName;

    @JsonProperty("achieved_at")
    private String achievedAt;

    @JsonProperty("detected_at")
    private String detectedAt;

    @JsonProperty("special_events")
    private List<ChangelogTag> specialEvents;
}
