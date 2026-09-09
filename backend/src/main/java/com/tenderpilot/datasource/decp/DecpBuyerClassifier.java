package com.tenderpilot.datasource.decp;

import com.tenderpilot.procurement.domain.BuyerCategory;

import java.text.Normalizer;
import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class DecpBuyerClassifier {

    public BuyerCategory classify(
            String buyerName
    ) {

        String value =
                normalize(buyerName);

        if (value == null) {
            return BuyerCategory.OTHER;
        }

        if (
                value.startsWith("ville ")
                ||
                value.equals("ville")
                ||
                value.contains("commune")
                ||
                value.contains("mairie")
                ||
                value.contains("departement")
                ||
                value.startsWith("region ")
                ||
                value.contains("metropole")
                ||
                value.contains("communaute ")
        ) {
            return BuyerCategory.LOCAL_AUTHORITY;
        }

        if (
                value.contains("ministere")
                ||
                value.startsWith("etat ")
        ) {
            return BuyerCategory.CENTRAL_GOVERNMENT;
        }

        if (
                value.contains("universite")
                ||
                value.contains("academie")
                ||
                value.contains("ecole")
        ) {
            return BuyerCategory.EDUCATION;
        }

        if (
                value.contains("hopital")
                ||
                value.contains("centre hospitalier")
                ||
                value.startsWith("chu ")
        ) {
            return BuyerCategory.HEALTHCARE;
        }

        if (
                value.contains(
                        "office public de l habitat"
                )
        ) {
            return BuyerCategory.SOCIAL_HOUSING;
        }

        if (
                value.contains("transport")
                ||
                value.contains("sncf")
        ) {
            return BuyerCategory.TRANSPORT;
        }

        return BuyerCategory.PUBLIC_AGENCY;
    }

    private String normalize(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value
                        .trim()
                        .replaceAll("\\s+", " ");

        if (cleaned.isBlank()) {
            return null;
        }

        return Normalizer
                .normalize(
                        cleaned,
                        Normalizer.Form.NFD
                )
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT);
    }
}
