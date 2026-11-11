package com.pachi.aredl.model.country;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryRank {

    private Integer rank;

    @JsonProperty("extremes_rank")
    private Integer extremesRank;

    @JsonProperty("level_points")
    private Integer levelPoints;
    private Integer extremes;
}
