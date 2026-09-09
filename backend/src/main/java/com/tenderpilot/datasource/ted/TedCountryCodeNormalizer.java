package com.tenderpilot.datasource.ted;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class TedCountryCodeNormalizer {

    private static final Map<String, String>
            ALPHA3_TO_ALPHA2 =

            Map.ofEntries(

                    Map.entry("AUT", "AT"),
                    Map.entry("BEL", "BE"),
                    Map.entry("BGR", "BG"),
                    Map.entry("HRV", "HR"),
                    Map.entry("CYP", "CY"),
                    Map.entry("CZE", "CZ"),
                    Map.entry("DNK", "DK"),
                    Map.entry("EST", "EE"),
                    Map.entry("FIN", "FI"),
                    Map.entry("FRA", "FR"),
                    Map.entry("DEU", "DE"),
                    Map.entry("GRC", "GR"),
                    Map.entry("HUN", "HU"),
                    Map.entry("IRL", "IE"),
                    Map.entry("ITA", "IT"),
                    Map.entry("LVA", "LV"),
                    Map.entry("LTU", "LT"),
                    Map.entry("LUX", "LU"),
                    Map.entry("MLT", "MT"),
                    Map.entry("NLD", "NL"),
                    Map.entry("POL", "PL"),
                    Map.entry("PRT", "PT"),
                    Map.entry("ROU", "RO"),
                    Map.entry("SVK", "SK"),
                    Map.entry("SVN", "SI"),
                    Map.entry("ESP", "ES"),
                    Map.entry("SWE", "SE"),

                    Map.entry("ISL", "IS"),
                    Map.entry("LIE", "LI"),
                    Map.entry("NOR", "NO"),

                    Map.entry("GBR", "GB"),
                    Map.entry("CHE", "CH")

            );


    public String normalize(
            String value
    ) {

        if (
                value == null
        ) {

            return null;

        }


        String normalized =

                value
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        if (
                normalized.length()
                        ==
                        2
                &&
                normalized.matches(
                        "[A-Z]{2}"
                )
        ) {

            return normalized;

        }


        return ALPHA3_TO_ALPHA2
                .get(
                        normalized
                );

    }

}
