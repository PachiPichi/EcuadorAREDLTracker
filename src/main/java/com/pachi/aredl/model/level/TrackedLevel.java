package com.pachi.aredl.model.level;

import com.pachi.aredl.model.country.CountryRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrackedLevel {
    private Level level;
    private CountryRecord firstVictor;
    private Integer dlecPosition;

    public boolean isCompleted(){
        return firstVictor != null;
    }
}
