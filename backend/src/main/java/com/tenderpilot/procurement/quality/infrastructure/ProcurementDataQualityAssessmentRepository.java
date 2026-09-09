package com.tenderpilot.procurement.quality.infrastructure;

import com.tenderpilot.procurement.quality.domain.ProcurementDataQualityAssessmentEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcurementDataQualityAssessmentRepository
        extends JpaRepository<
                ProcurementDataQualityAssessmentEntity,
                UUID
        > {

    Optional<ProcurementDataQualityAssessmentEntity>
    findByTender_Id(
            UUID tenderId
    );

}
