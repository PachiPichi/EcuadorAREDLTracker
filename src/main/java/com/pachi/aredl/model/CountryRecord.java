package com.pachi.aredl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryRecord {

    private String id;
    private Level level;

    @JsonProperty("submitted_by")
    private Player submittedBy;

    @JsonProperty("completion_count")
    private Integer completionCount;

    @JsonProperty("achieved_at")
    private String achievedAt;

    @JsonProperty("video_url")
    private String videoUrl;

    @JsonProperty("hide_video")
    private Boolean hideVideo;
}
