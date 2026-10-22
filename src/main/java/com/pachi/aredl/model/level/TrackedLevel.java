package com.pachi.aredl.model.level;

import com.pachi.aredl.model.country.CountryRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackedLevel {

    private Level level;
    private List<CountryRecord> records;
    private Integer dlecPosition;

    public boolean isCompleted(){
        return records != null && !records.isEmpty();
    }

    public CountryRecord getFirstRecord(){
        return isCompleted() ? records.getFirst() : null;
    }
}
