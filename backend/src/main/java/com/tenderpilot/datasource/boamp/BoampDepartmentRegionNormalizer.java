package com.tenderpilot.datasource.boamp;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class BoampDepartmentRegionNormalizer {

    private static final Map<String, Region>
            REGIONS =
            buildRegions();


    public Region normalize(
            String rawDepartment
    ) {

        String department =
                normalizeDepartment(
                        rawDepartment
                );


        if (
                department == null
        ) {

            return null;

        }


        Region known =
                REGIONS.get(
                        department
                );


        if (
                known != null
        ) {

            return known;

        }


        return new Region(

                department,

                "FR",

                "Département "
                        +
                        department

        );

    }


    private String normalizeDepartment(
            String raw
    ) {

        if (
                raw == null
        ) {

            return null;

        }


        String value =

                raw
                        .trim()
                        .toUpperCase(
                                Locale.ROOT
                        );


        if (
                value.isBlank()
        ) {

            return null;

        }


        if (
                value.matches(
                        "\\d"
                )
        ) {

            value =
                    "0"
                            +
                            value;

        }


        return value;

    }


    private static Map<String, Region> buildRegions() {

        Map<String, Region> map =
                new HashMap<>();


        add(

                map,

                "IDF",

                "Île-de-France",

                "75",
                "77",
                "78",
                "91",
                "92",
                "93",
                "94",
                "95"

        );


        add(

                map,

                "HDF",

                "Hauts-de-France",

                "02",
                "59",
                "60",
                "62",
                "80"

        );


        add(

                map,

                "NOR",

                "Normandie",

                "14",
                "27",
                "50",
                "61",
                "76"

        );


        add(

                map,

                "BRE",

                "Bretagne",

                "22",
                "29",
                "35",
                "56"

        );


        add(

                map,

                "PDL",

                "Pays de la Loire",

                "44",
                "49",
                "53",
                "72",
                "85"

        );


        add(

                map,

                "CVL",

                "Centre-Val de Loire",

                "18",
                "28",
                "36",
                "37",
                "41",
                "45"

        );


        add(

                map,

                "GES",

                "Grand Est",

                "08",
                "10",
                "51",
                "52",
                "54",
                "55",
                "57",
                "67",
                "68",
                "88"

        );


        add(

                map,

                "BFC",

                "Bourgogne-Franche-Comté",

                "21",
                "25",
                "39",
                "58",
                "70",
                "71",
                "89",
                "90"

        );


        add(

                map,

                "NAQ",

                "Nouvelle-Aquitaine",

                "16",
                "17",
                "19",
                "23",
                "24",
                "33",
                "40",
                "47",
                "64",
                "79",
                "86",
                "87"

        );


        add(

                map,

                "OCC",

                "Occitanie",

                "09",
                "11",
                "12",
                "30",
                "31",
                "32",
                "34",
                "46",
                "48",
                "65",
                "66",
                "81",
                "82"

        );


        add(

                map,

                "ARA",

                "Auvergne-Rhône-Alpes",

                "01",
                "03",
                "07",
                "15",
                "26",
                "38",
                "42",
                "43",
                "63",
                "69",
                "73",
                "74"

        );


        add(

                map,

                "PACA",

                "Provence-Alpes-Côte d'Azur",

                "04",
                "05",
                "06",
                "13",
                "83",
                "84"

        );


        add(

                map,

                "COR",

                "Corse",

                "2A",
                "2B"

        );


        add(
                map,
                "GUA",
                "Guadeloupe",
                "971"
        );


        add(
                map,
                "MTQ",
                "Martinique",
                "972"
        );


        add(
                map,
                "GUF",
                "Guyane",
                "973"
        );


        add(
                map,
                "REU",
                "La Réunion",
                "974"
        );


        add(
                map,
                "MAY",
                "Mayotte",
                "976"
        );


        return Map.copyOf(
                map
        );

    }


    private static void add(

            Map<String, Region> map,

            String regionCode,

            String regionName,

            String... departments

    ) {

        for (
                String department :
                departments
        ) {

            map.put(

                    department,

                    new Region(

                            department,

                            regionCode,

                            regionName

                    )

            );

        }

    }


    public record Region(

            String departmentCode,

            String regionCode,

            String regionName

    ) {
    }

}
