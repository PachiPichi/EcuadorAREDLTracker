package com.pachi.aredl.model.country;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountryRank {

    private Integer rank;
    private Integer extremes_rank;
    private Integer level_points;
    private Integer extremes;
}
