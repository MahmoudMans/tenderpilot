package com.tenderpilot.datasource.decp;

public interface DecpSearchGateway {

    DecpSearchPage search(
            DecpSearchRequest request
    );
}
