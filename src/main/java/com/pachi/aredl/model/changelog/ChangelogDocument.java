package com.pachi.aredl.model.changelog;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangelogDocument {

    @JsonProperty("schema_version")
    private int schemaVersion = 1;

    private boolean initialized;

    private List<ChangelogEvent> events = new ArrayList<>();

    private ChangelogState state = new ChangelogState();
}
