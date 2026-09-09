package com.tenderpilot.procurement.award.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AwardSupplierValue {

    @Column(name = "supplier_identifier", length = 200)
    private String identifier;

    @Column(name = "identifier_type", length = 100)
    private String identifierType;

    @Column(name = "legal_name", length = 300)
    private String legalName;

    protected AwardSupplierValue() {
    }

    public AwardSupplierValue(
            String identifier,
            String identifierType,
            String legalName
    ) {
        this.identifier = identifier;
        this.identifierType = identifierType;
        this.legalName = legalName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getIdentifierType() {
        return identifierType;
    }

    public String getLegalName() {
        return legalName;
    }
}
