package com.tenderpilot.datasource.boamp;

public interface BoampSearchGateway {

    BoampSearchPage search(
            BoampSearchRequest request
    );

}
