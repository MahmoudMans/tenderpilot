package com.tenderpilot.datasource.ted;

public record TedSearchRequest(

        String expertQuery,

        int page,

        int limit

) {
}
