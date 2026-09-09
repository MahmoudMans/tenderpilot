package com.tenderpilot.datasource.ted;

import java.util.List;

public record TedSearchPage(

        long total,

        List<TedSearchNotice> notices

) {
}
