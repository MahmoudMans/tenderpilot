package com.tenderpilot.datasource.decp;

import com.tenderpilot.datasource.boamp.BoampDepartmentRegionNormalizer;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class DecpExecutionLocationNormalizer {

    private final BoampDepartmentRegionNormalizer
            departmentRegionNormalizer;

    public DecpExecutionLocationNormalizer(
            BoampDepartmentRegionNormalizer departmentRegionNormalizer
    ) {
        this.departmentRegionNormalizer =
                departmentRegionNormalizer;
    }

    public NormalizedLocation normalize(
            String rawCode,
            String rawType,
            String rawName
    ) {

        String code = clean(rawCode);
        String name = clean(rawName);

        if (
                code == null
                &&
                name == null
        ) {
            return null;
        }

        String department =
                deriveDepartment(code);

        BoampDepartmentRegionNormalizer.Region region =
                departmentRegionNormalizer
                        .normalize(department);

        String city =
                isMunicipalType(rawType)
                        ? name
                        : null;

        if (region == null) {
            return new NormalizedLocation(
                    "FR",
                    department,
                    name,
                    city
            );
        }

        return new NormalizedLocation(
                "FR",
                region.regionCode(),
                region.regionName(),
                city
        );
    }

    private String deriveDepartment(
            String code
    ) {

        if (code == null) {
            return null;
        }

        String normalized =
                code.toUpperCase(Locale.ROOT);

        if (
                normalized.startsWith("2A")
                ||
                normalized.startsWith("2B")
        ) {
            return normalized.substring(0, 2);
        }

        String digits =
                normalized.replaceAll(
                        "[^0-9]",
                        ""
                );

        if (
                digits.length() >= 3
                &&
                (
                        digits.startsWith("97")
                        ||
                        digits.startsWith("98")
                )
        ) {
            return digits.substring(0, 3);
        }

        if (digits.length() >= 2) {
            return digits.substring(0, 2);
        }

        return digits.isBlank()
                ? null
                : digits;
    }

    private boolean isMunicipalType(
            String value
    ) {

        String cleaned = clean(value);

        if (cleaned == null) {
            return false;
        }

        String normalized =
                cleaned.toLowerCase(Locale.ROOT);

        return normalized.contains("commune")
                ||
                normalized.contains("postal");
    }

    private String clean(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.trim();

        return cleaned.isBlank()
                ? null
                : cleaned;
    }

    public record NormalizedLocation(
            String countryCode,
            String regionCode,
            String regionName,
            String city
    ) {
    }
}
