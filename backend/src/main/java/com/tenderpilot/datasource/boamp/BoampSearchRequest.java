package com.tenderpilot.datasource.boamp;

public record BoampSearchRequest(

        String where,

        int offset,

        int limit

) {
}
