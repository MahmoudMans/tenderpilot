package com.tenderpilot.procurement.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "procurement_tender_lots"
)
public class TenderLotEntity {

    @Id
    private UUID id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "tender_id",
            nullable = false
    )
    private TenderEntity tender;


    @Column(
            name = "lot_number",
            nullable = false,
            length = 80
    )
    private String lotNumber;


    @Column(
            name = "title",
            nullable = false,
            length = 500
    )
    private String title;


    @Column(
            name = "description",
            nullable = false,
            length = 5000
    )
    private String description;


    @Column(
            name = "estimated_min_value",
            precision = 19,
            scale = 2
    )
    private BigDecimal estimatedMinValue;


    @Column(
            name = "estimated_max_value",
            precision = 19,
            scale = 2
    )
    private BigDecimal estimatedMaxValue;


    @Column(
            name = "currency",
            length = 3
    )
    private String currency;


    @ElementCollection(
            fetch = FetchType.LAZY
    )
    @CollectionTable(
            name = "procurement_tender_lot_cpvs",
            joinColumns = @JoinColumn(
                    name = "lot_id"
            )
    )
    @OrderColumn(
            name = "position"
    )
    private List<CpvClassificationValue> cpvs =
            new ArrayList<>();


    protected TenderLotEntity() {
    }


    private TenderLotEntity(

            TenderEntity tender,

            String lotNumber,

            String title,

            String description,

            BigDecimal estimatedMinValue,

            BigDecimal estimatedMaxValue,

            String currency,

            List<CpvClassificationValue> cpvs

    ) {

        this.id =
                UUID.randomUUID();

        this.tender =
                tender;

        this.lotNumber =
                lotNumber;

        this.title =
                title;

        this.description =
                description;

        this.estimatedMinValue =
                estimatedMinValue;

        this.estimatedMaxValue =
                estimatedMaxValue;

        this.currency =
                currency;

        this.cpvs.addAll(
                cpvs
        );

    }


    public static TenderLotEntity create(

            TenderEntity tender,

            String lotNumber,

            String title,

            String description,

            BigDecimal estimatedMinValue,

            BigDecimal estimatedMaxValue,

            String currency,

            List<CpvClassificationValue> cpvs

    ) {

        return new TenderLotEntity(

                tender,

                lotNumber,

                title,

                description,

                estimatedMinValue,

                estimatedMaxValue,

                currency,

                cpvs

        );

    }


    public UUID getId() {

        return id;

    }


    public String getLotNumber() {

        return lotNumber;

    }


    public String getTitle() {

        return title;

    }


    public String getDescription() {

        return description;

    }


    public BigDecimal getEstimatedMinValue() {

        return estimatedMinValue;

    }


    public BigDecimal getEstimatedMaxValue() {

        return estimatedMaxValue;

    }


    public String getCurrency() {

        return currency;

    }


    public List<CpvClassificationValue> getCpvs() {

        return List.copyOf(
                cpvs
        );

    }

}
