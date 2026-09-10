package com.tenderpilot.biddecision.infrastructure;

import com.tenderpilot.biddecision.domain.BidDecisionHistoryEntity;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BidDecisionHistoryRepository
        extends JpaRepository<
                BidDecisionHistoryEntity,
                UUID
        > {

    List<BidDecisionHistoryEntity>
    findByDecision_IdOrderByCreatedAtAsc(
            UUID decisionId
    );

}
