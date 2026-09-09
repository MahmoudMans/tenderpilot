package com.tenderpilot.datasource.boamp;

import java.util.List;

public record BoampSearchPage(

        long total,

        List<BoampNotice> notices

) {
}
