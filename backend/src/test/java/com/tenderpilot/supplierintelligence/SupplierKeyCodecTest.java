package com.tenderpilot.supplierintelligence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tenderpilot.common.domain.InvalidRequestException;

import com.tenderpilot.supplierintelligence.application.SupplierKeyCodec;

import org.junit.jupiter.api.Test;

class SupplierKeyCodecTest {

    private final SupplierKeyCodec codec =
            new SupplierKeyCodec();


    @Test
    void supplierNameRoundTripsThroughUrlSafeKey() {

        String key =
                codec.encode(
                        "Entreprise Générale Bâtiment"
                );


        assertThat(
                key
        )
                .doesNotContain(
                        "/"
                )
                .doesNotContain(
                        "+"
                );


        assertThat(
                codec.decode(
                        key
                )
        )
                .isEqualTo(
                        "Entreprise Générale Bâtiment"
                );
    }


    @Test
    void whitespaceIsNormalizedBeforeEncoding() {

        String key =
                codec.encode(
                        "  Supplier    Alpha   France "
                );


        assertThat(
                codec.decode(
                        key
                )
        )
                .isEqualTo(
                        "Supplier Alpha France"
                );
    }


    @Test
    void invalidKeyIsRejected() {

        assertThatThrownBy(
                () ->
                        codec.decode(
                                "###invalid###"
                        )
        )
                .isInstanceOf(
                        InvalidRequestException.class
                );
    }

}
