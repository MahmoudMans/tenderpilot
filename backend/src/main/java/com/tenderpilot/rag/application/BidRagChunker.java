package com.tenderpilot.rag.application;

import com.tenderpilot.rag.domain.BidRagChunk;

import com.tenderpilot.rag.infrastructure.RagSettings;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class BidRagChunker {

    private final RagSettings settings;


    public BidRagChunker(
            RagSettings settings
    ) {

        this.settings =
                settings;
    }


    public List<BidRagChunk> chunk(
            String rawText
    ) {

        String text =
                normalize(
                        rawText
                );


        if (
                text.isBlank()
        ) {

            return List.of();
        }


        List<BidRagChunk> chunks =
                new ArrayList<>();


        int start =
                0;

        int index =
                0;


        while (
                start < text.length()
        ) {

            int desiredEnd =
                    Math.min(
                            text.length(),
                            start
                                    +
                                    settings.chunkSizeChars()
                    );


            int end =
                    chooseBoundary(
                            text,
                            start,
                            desiredEnd
                    );


            if (
                    end <= start
            ) {

                end =
                        desiredEnd;
            }


            String value =
                    text.substring(
                            start,
                            end
                    )
                            .trim();


            if (
                    !value.isBlank()
            ) {

                chunks.add(
                        new BidRagChunk(
                                index,
                                start,
                                end,
                                value
                        )
                );


                index++;
            }


            if (
                    end >= text.length()
            ) {

                break;
            }


            int nextStart =
                    end
                            -
                            settings.chunkOverlapChars();


            if (
                    nextStart <= start
            ) {

                nextStart =
                        start + 1;
            }


            start =
                    nextStart;
        }


        return List.copyOf(
                chunks
        );
    }


    private int chooseBoundary(
            String text,
            int start,
            int desiredEnd
    ) {

        if (
                desiredEnd >= text.length()
        ) {

            return text.length();
        }


        int minimum =
                start
                        +
                        settings.chunkSizeChars()
                        /
                        2;


        int paragraph =
                text.lastIndexOf(
                        "\n\n",
                        desiredEnd
                );


        if (
                paragraph >= minimum
        ) {

            return paragraph;
        }


        int sentence =
                Math.max(
                        text.lastIndexOf(
                                ". ",
                                desiredEnd
                        ),
                        text.lastIndexOf(
                                "\n",
                                desiredEnd
                        )
                );


        if (
                sentence >= minimum
        ) {

            return sentence + 1;
        }


        int whitespace =
                text.lastIndexOf(
                        ' ',
                        desiredEnd
                );


        if (
                whitespace >= minimum
        ) {

            return whitespace;
        }


        return desiredEnd;
    }


    private String normalize(
            String value
    ) {

        if (
                value == null
        ) {

            return "";
        }


        return value
                .replace(
                        "\r\n",
                        "\n"
                )
                .replace(
                        '\r',
                        '\n'
                )
                .replaceAll(
                        "[\\t\\x0B\\f]+",
                        " "
                )
                .replaceAll(
                        " +",
                        " "
                )
                .replaceAll(
                        "\\n{3,}",
                        "\n\n"
                )
                .trim();
    }

}
