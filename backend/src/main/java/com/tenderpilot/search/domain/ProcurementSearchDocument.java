package com.tenderpilot.search.domain;

import java.math.BigDecimal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ProcurementSearchDocument(

        UUID id,

        String canonicalKey,

        String title,

        String description,

        String buyerName,

        String buyerCategory,

        String status,

        String procedureType,

        String contractNature,

        String publicationDate,

        String submissionDeadline,

        BigDecimal estimatedMinValue,

        BigDecimal estimatedMaxValue,

        String currency,

        String countryCode,

        String regionCode,

        String regionName,

        boolean frameworkAgreement,

        boolean jointProcurement,

        boolean electronicSubmission,

        List<String> cpvCodes,

        List<String> cpvLabels,

        List<String> sources,

        List<String> lotTitles,

        BigDecimal awardedAmount,

        String awardedCurrency,

        List<String> supplierNames,

        List<String> supplierIdentifiers,

        Integer qualityScore,

        String qualityGrade,

        List<String> qualityIssueCodes

) {

    public Map<String, Object> toSource() {

        Map<String, Object> source =
                new LinkedHashMap<>();

        source.put(
                "id",
                id.toString()
        );

        source.put(
                "canonicalKey",
                canonicalKey
        );

        source.put(
                "title",
                title
        );

        source.put(
                "description",
                description
        );

        source.put(
                "buyerName",
                buyerName
        );

        source.put(
                "buyerCategory",
                buyerCategory
        );

        source.put(
                "status",
                status
        );

        source.put(
                "procedureType",
                procedureType
        );

        source.put(
                "contractNature",
                contractNature
        );

        source.put(
                "publicationDate",
                publicationDate
        );

        source.put(
                "submissionDeadline",
                submissionDeadline
        );

        source.put(
                "estimatedMinValue",
                estimatedMinValue
        );

        source.put(
                "estimatedMaxValue",
                estimatedMaxValue
        );

        source.put(
                "currency",
                currency
        );

        source.put(
                "countryCode",
                countryCode
        );

        source.put(
                "regionCode",
                regionCode
        );

        source.put(
                "regionName",
                regionName
        );

        source.put(
                "frameworkAgreement",
                frameworkAgreement
        );

        source.put(
                "jointProcurement",
                jointProcurement
        );

        source.put(
                "electronicSubmission",
                electronicSubmission
        );

        source.put(
                "cpvCodes",
                cpvCodes
        );

        source.put(
                "cpvLabels",
                cpvLabels
        );

        source.put(
                "sources",
                sources
        );

        source.put(
                "lotTitles",
                lotTitles
        );

        source.put(
                "awardedAmount",
                awardedAmount
        );

        source.put(
                "awardedCurrency",
                awardedCurrency
        );

        source.put(
                "supplierNames",
                supplierNames
        );

        source.put(
                "supplierIdentifiers",
                supplierIdentifiers
        );

        source.put(
                "qualityScore",
                qualityScore
        );

        source.put(
                "qualityGrade",
                qualityGrade
        );

        source.put(
                "qualityIssueCodes",
                qualityIssueCodes
        );


        return source;

    }

}
