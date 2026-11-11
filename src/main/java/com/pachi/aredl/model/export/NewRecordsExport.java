package com.pachi.aredl.model.export;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.pachi.aredl.model.changelog.ChangelogEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class NewRecordsExport {

    @JsonProperty("generated_at")
    private final String generatedAt;

    private final List<ChangelogEvent> records;
}
