package com.tenderpilot.rag.application;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class CosineSimilarity {

    public double score(
            List<Double> left,
            List<Double> right
    ) {

        if (
                left == null
                ||
                right == null
                ||
                left.isEmpty()
                ||
                right.isEmpty()
        ) {

            throw new IllegalArgumentException(
                    "Embeddings cannot be empty"
            );
        }


        if (
                left.size()
                        !=
                        right.size()
        ) {

            throw new IllegalArgumentException(
                    "Embedding dimensions do not match"
            );
        }


        double dot =
                0;

        double leftMagnitude =
                0;

        double rightMagnitude =
                0;


        for (
                int index = 0;
                index < left.size();
                index++
        ) {

            double a =
                    left.get(
                            index
                    );

            double b =
                    right.get(
                            index
                    );


            dot +=
                    a * b;

            leftMagnitude +=
                    a * a;

            rightMagnitude +=
                    b * b;
        }


        if (
                leftMagnitude == 0
                ||
                rightMagnitude == 0
        ) {

            return 0;
        }


        return dot
                /
                (
                        Math.sqrt(
                                leftMagnitude
                        )
                        *
                        Math.sqrt(
                                rightMagnitude
                        )
                );
    }

}
