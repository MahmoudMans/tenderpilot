package com.tenderpilot.localai.application;

import com.tenderpilot.localai.domain.BidDocumentExtractionResult;

public interface BidDocumentTextExtractor {

    BidDocumentExtractionResult extract(
            String filename,
            byte[] content
    );

}
