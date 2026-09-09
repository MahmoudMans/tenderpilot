package com.tenderpilot.datasource.boamp;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Component;

@Component
public class BoampQueryFactory {

    private final Clock clock;


    public BoampQueryFactory() {

        this(
                Clock.systemUTC()
        );

    }


    BoampQueryFactory(
            Clock clock
    ) {

        this.clock =
                clock;

    }


    public String recentPublications(
            int lookbackDays
    ) {

        int bounded =

                Math.max(

                        1,

                        Math.min(
                                31,
                                lookbackDays
                        )

                );


        LocalDate end =
                LocalDate.now(
                        clock
                );


        LocalDate start =
                end.minusDays(
                        bounded
                                -
                                1L
                );


        return "dateparution IN [date'"
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
