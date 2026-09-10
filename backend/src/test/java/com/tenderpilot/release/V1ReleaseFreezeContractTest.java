package com.tenderpilot.release;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class V1ReleaseFreezeContractTest {

    @Test
    void projectAndRuntimeVersionsAreStableOnePointZero()
            throws Exception {

        String pom =
                read(
                        "backend/pom.xml"
                );


        String application =
                read(
                        "backend/src/main/resources/application.yml"
                );


        assertThat(
                pom
        )
                .containsPattern(
                        "<version>\\s*1\\.0\\.0\\s*</version>"
                );


        assertThat(
                application
        )
                .contains(
                        "version: 1.0.0"
                );
    }


    @Test
    void releaseManifestFreezesTheAuthoritativeV1Baseline()
            throws Exception {

        String manifest =
                read(
                        "release/tenderpilot-v1-release.json"
                );


        assertThat(
                manifest
        )
                .contains(
                        "\"product\": \"TenderPilot\"",
                        "\"version\": \"1.0.0\"",
                        "\"releaseTag\": \"v1.0.0\"",
                        "\"releaseType\": \"stable\"",
                        "\"featureFreezeBase\": \"60a2459\"",
                        "\"latestLiquibaseMigration\": \"015\"",
                        "\"minimumBackendTests\": 222",
                        "\"releaseContractTests\": 26",
                        "\"featureDevelopmentComplete\": true",
                        "\"ciCdRequired\": false",
                        "\"paidServiceRequired\": false"
                );
    }


    @Test
    void databaseMigrationHistoryRemainsFrozenAtFifteen()
            throws Exception {

        Path changes =
                repositoryRoot()
                        .resolve(
                                "backend/src/main/resources/db/changelog/changes"
                        );


        Pattern prefix =
                Pattern.compile(
                        "^(\\d+)-"
                );


        int latest;


        try (
                var stream =
                        Files.list(
                                changes
                        )
        ) {

            latest =
                    stream
                            .filter(
                                    Files::isRegularFile
                            )
                            .map(
                                    path ->
                                            path
                                                    .getFileName()
                                                    .toString()
                            )
                            .map(
                                    prefix::matcher
                            )
                            .filter(
                                    Matcher::find
                            )
                            .map(
                                    matcher ->
                                            Integer.parseInt(
                                                    matcher.group(
                                                            1
                                                    )
                                            )
                            )
                            .max(
                                    Comparator.naturalOrder()
                            )
                            .orElseThrow();
        }


        assertThat(
                latest
        )
                .isEqualTo(
                        15
                );
    }


    @Test
    void portfolioDemonstrationRemainsSyntheticAtReleaseFreeze()
            throws Exception {

        String dataset =
                read(
                        "frontend/src/features/showcase/"
                                +
                                "showcase-data.ts"
                );


        String capture =
                read(
                        "frontend/src/app/(capture)/showcase/"
                                +
                                "capture/page.tsx"
                );


        assertThat(
                dataset
        )
                .contains(
                        "SYNTHETIC_NON_PRODUCTION",
                        "tenderpilot-portfolio-showcase-v1",
                        "generatedFromLiveData:",
                        "false",
                        "persistent:",
                        "false"
                );


        assertThat(
                capture
        )
                .contains(
                        "data-synthetic-data=\"true\""
                )
                .doesNotContain(
                        "useAuth",
                        "fetch(",
                        "/api/v1/",
                        "Date.now",
                        "Math.random"
                );
    }


    @Test
    void finalLocalCertificationScriptsArePresent()
            throws Exception {

        Path root =
                repositoryRoot();


        Path releaseGate =
                root.resolve(
                        "scripts/release-gate.sh"
                );


        Path v1Gate =
                root.resolve(
                        "scripts/v1-readiness-gate.sh"
                );


        assertThat(
                Files.isRegularFile(
                        releaseGate
                )
        )
                .isTrue();


        assertThat(
                Files.isRegularFile(
                        v1Gate
                )
        )
                .isTrue();


        assertThat(
                Files.readString(
                        releaseGate
                )
        )
                .contains(
                        "1.0.0",
                        "222",
                        "RELEASE_GATE_STATUS=PASS"
                );


        assertThat(
                Files.readString(
                        v1Gate
                )
        )
                .contains(
                        "v1.0.0",
                        "V1_READINESS_STATUS=PASS"
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
