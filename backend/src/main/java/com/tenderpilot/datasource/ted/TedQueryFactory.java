package com.tenderpilot.datasource.ted;

import java.time.Clock;
import java.time.LocalDate;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class TedQueryFactory {

    private static final DateTimeFormatter
            TED_DATE =

            DateTimeFormatter
                    .BASIC_ISO_DATE;


    private final Clock clock;


    public TedQueryFactory() {

        this(
                Clock.systemUTC()
        );

    }


    TedQueryFactory(
            Clock clock
    ) {

        this.clock =
                clock;

    }


    public String recentPublications(
            int lookbackDays
    ) {

        int boundedDays =

                Math.max(

                        1,

                        Math.min(
                                lookbackDays,
                                31
                        )

                );


        LocalDate end =
                LocalDate.now(
                        clock
                );


        LocalDate start =

                end.minusDays(
                        boundedDays
                                -
                                1L
                );


        return "PD = ("
                +
                TED_DATE.format(
                        start
                )
                +
                " <> "
                +
                TED_DATE.format(
                        end
                )
                +
                ")";

    }

}
