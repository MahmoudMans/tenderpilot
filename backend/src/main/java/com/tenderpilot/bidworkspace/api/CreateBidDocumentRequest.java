package com.tenderpilot.bidworkspace.api;

import com.tenderpilot.bidworkspace.domain.BidDocumentCategory;

public record CreateBidDocumentRequest(

        String originalFilename,

        String contentType,

        String contentBase64,

        BidDocumentCategory category,

        String note

) {
}
