package com.pachi.aredl.model.changelog;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogRecordState {

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

    @JsonProperty("achieved_at")
    private String achievedAt;
}
