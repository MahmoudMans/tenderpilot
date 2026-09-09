package com.tenderpilot.datasource.ted;

public interface TedSearchGateway {

    TedSearchPage search(
            TedSearchRequest request
    );

}
