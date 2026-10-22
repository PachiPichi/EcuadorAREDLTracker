package com.pachi.aredl.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {

    private String id;
    private String username;

    private String global_name;
    private String discord_id;
    private String discord_avatar;
}
