package com.pachi.aredl.model.export;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VictorExport {

    @JsonProperty("record_id")
    private final String recordId;

    @JsonProperty("player_id")
    private final String playerId;

    private final String username;

    @JsonProperty("global_name")
    private final String globalName;

    @JsonProperty("discord_id")
    private final String discordId;

    @JsonProperty("discord_avatar")
    private final String discordAvatar;

    @JsonProperty("completion_count")
    private final Integer completionCount;

    @JsonProperty("achieved_at")
    private final String achievedAt;

    @JsonProperty("video_url")
    private final String videoUrl;

    @JsonProperty("hide_video")
    private final Boolean hideVideo;
}
