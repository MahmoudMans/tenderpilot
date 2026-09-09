package com.tenderpilot.datasource.decp;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class DecpQueryFactory {

    private final Clock clock;

    public DecpQueryFactory() {
        this(Clock.systemUTC());
    }

    DecpQueryFactory(Clock clock) {
        this.clock = clock;
    }

    public String recentPublications(
            int lookbackDays
    ) {

        int bounded =
                Math.max(
                        1,
                        Math.min(31, lookbackDays)
                );

        LocalDate end =
                LocalDate.now(clock);

        LocalDate start =
                end.minusDays(
                        bounded - 1L
                );

        return "datepublicationdonnees IN [date'"
                +
                start
                +
                "'..date'"
                +
                end
                +
                "']";
    }
}
