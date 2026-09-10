package com.tenderpilot.rag.infrastructure;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class RagEmbeddingCodec {

    public String encode(
            List<Double> values
    ) {

        if (
                values == null
                ||
                values.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Embedding cannot be empty"
            );
        }


        StringBuilder result =
                new StringBuilder();


        for (
                int index = 0;
                index < values.size();
                index++
        ) {

            if (
                    index > 0
            ) {

                result.append(
                        ','
                );
            }


            result.append(
                    Double.toString(
                            values.get(
                                    index
                            )
                    )
            );
        }


        return result.toString();
    }


    public List<Double> decode(
            String encoded
    ) {

        if (
                encoded == null
                ||
                encoded.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Encoded embedding cannot be empty"
            );
        }


        String[] parts =
                encoded.split(
                        ","
                );


        List<Double> values =
                new ArrayList<>(
                        parts.length
                );


        for (
                String part :
                parts
        ) {

            values.add(
                    Double.parseDouble(
                            part
                    )
            );
        }


        return List.copyOf(
                values
        );
    }

}
