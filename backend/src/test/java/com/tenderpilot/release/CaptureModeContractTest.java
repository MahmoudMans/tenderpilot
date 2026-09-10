package com.tenderpilot.release;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class CaptureModeContractTest {

    @Test
    void captureManifestDefinesSixDeterministicPortfolioFrames()
            throws Exception {

        String manifest =
                read(
                        "frontend/src/features/showcase/"
                                +
                                "capture-manifest.ts"
                );


        assertThat(
                occurrences(
                        manifest,
                        "recommendedFilename:"
                )
        )
                .isEqualTo(
                        6
                );


        assertThat(
                manifest
        )
                .contains(
                        "CAPTURE_WIDTH",
                        "1440",
                        "CAPTURE_HEIGHT",
                        "900",
                        "executive",
                        "pipeline",
                        "market",
                        "alerts",
                        "operations",
                        "architecture"
                );
    }


    @Test
    void recommendedCaptureFilenamesAreUniquePngFiles()
            throws Exception {

        String manifest =
                read(
                        "frontend/src/features/showcase/"
                                +
                                "capture-manifest.ts"
                );


        Pattern pattern =
                Pattern.compile(
                        "recommendedFilename:\\s*\\n\\s*\"([^\"]+)\""
                );


        Matcher matcher =
                pattern.matcher(
                        manifest
                );


        HashSet<String> filenames =
                new HashSet<>();


        while (
                matcher.find()
        ) {

            String filename =
                    matcher.group(
                            1
                    );


            assertThat(
                    filename
            )
                    .endsWith(
                            ".png"
                    );


            assertThat(
                    filenames.add(
                            filename
                    )
            )
                    .as(
                            "unique capture filename %s",
                            filename
                    )
                    .isTrue();
        }


        assertThat(
                filenames
        )
                .hasSize(
                        6
                );
    }


    @Test
    void captureRouteIsStaticSyntheticAndCannotReadLiveApis()
            throws Exception {

        String page =
                read(
                        "frontend/src/app/(capture)/showcase/"
                                +
                                "capture/page.tsx"
                );


        assertThat(
                page
        )
                .contains(
                        "data-capture-mode=\"portfolio\"",
                        "data-synthetic-data=\"true\"",
                        "Synthetic non-production presentation data",
                        "CAPTURE_DATASET_ID"
                )
                .doesNotContain(
                        "\"use client\"",
                        "useAuth",
                        "fetch(",
                        "axios",
                        "\"/api/",
                        "`/api/",
                        "localStorage",
                        "sessionStorage",
                        "Date.now",
                        "Math.random"
                );
    }


    @Test
    void captureModeIsDiscoverableFromProductNavigationAndShowcase()
            throws Exception {

        String navigation =
                read(
                        "frontend/src/features/navigation/"
                                +
                                "product-navigation.ts"
                );


        String showcase =
                read(
                        "frontend/src/app/(workspace)/showcase/"
                                +
                                "page.tsx"
                );


        assertThat(
                navigation
        )
                .contains(
                        "\"/showcase/capture\"",
                        "Portfolio capture mode"
                );


        assertThat(
                showcase
        )
                .contains(
                        "href=\"/showcase/capture\"",
                        "Open capture mode"
                );
    }


    @Test
    void releaseGateCoversCaptureRouteAndCurrentVersion()
            throws Exception {

        String gate =
                read(
                        "scripts/release-gate.sh"
                );


        assertThat(
                gate
        )
                .contains(
                        "1.0.0",
                        "\"/showcase/capture\"",
                        "showcase/capture/page.tsx",
                        "222"
                );
    }


    private String read(
            String relative
    )
            throws Exception {

        return Files.readString(
                repositoryRoot()
                        .resolve(
                                relative
                        )
        );
    }


    private int occurrences(
            String source,
            String token
    ) {

        int count =
                0;


        int offset =
                0;


        while (
                true
        ) {

            int index =
                    source.indexOf(
                            token,
                            offset
                    );


            if (
                    index < 0
            ) {

                return count;
            }


            count++;


            offset =
                    index
                            +
                            token.length();
        }
    }


    private Path repositoryRoot() {

        Path current =
                Path.of(
                        ""
                )
                        .toAbsolutePath()
                        .normalize();


        if (
                Files.isDirectory(
                        current.resolve(
                                "backend"
                        )
                )
                &&
                Files.isDirectory(
                        current.resolve(
                                "frontend"
                        )
                )
        ) {

            return current;
        }


        Path parent =
                current.getParent();


        if (
                parent != null
                &&
                Files.isDirectory(
                        parent.resolve(
                                "backend"
                        )
                )
                &&
                Files.isDirectory(
                        parent.resolve(
                                "frontend"
                        )
                )
        ) {

            return parent;
        }


        throw new IllegalStateException(
                "Unable to locate TenderPilot repository root"
        );
    }

}
