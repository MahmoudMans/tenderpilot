package com.tenderpilot.release;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class ShowcaseContractTest {

    @Test
    void showcaseDatasetIsExplicitlySyntheticAndNonPersistent()
            throws Exception {

        String data =
                Files.readString(
                        repositoryRoot()
                                .resolve(
                                        "frontend/src/features/showcase/"
                                                +
                                                "showcase-data.ts"
                                )
                );


        assertThat(
                data
        )
                .contains(
                        "SYNTHETIC_NON_PRODUCTION",
                        "tenderpilot-portfolio-showcase-v1",
                        "generatedFromLiveData:",
                        "false",
                        "persistent:",
                        "false"
                );
    }


    @Test
    void showcaseCannotCallLiveTenderPilotApis()
            throws Exception {

        Path root =
                repositoryRoot();


        String data =
                Files.readString(
                        root.resolve(
                                "frontend/src/features/showcase/"
                                        +
                                        "showcase-data.ts"
                        )
                );


        String page =
                Files.readString(
                        root.resolve(
                                "frontend/src/app/(workspace)/"
                                        +
                                        "showcase/page.tsx"
                        )
                );


        String combined =
                data
                        +
                        "\n"
                        +
                        page;


        assertThat(
                combined
        )
                .doesNotContain(
                        "useAuth",
                        "fetch(",
                        "axios",
                        "\"/api/",
                        "`/api/",
                        "Date.now",
                        "Math.random"
                );
    }


    @Test
    void showcaseCoversTheCoreProductStory()
            throws Exception {

        String page =
                Files.readString(
                        repositoryRoot()
                                .resolve(
                                        "frontend/src/app/(workspace)/"
                                                +
                                                "showcase/page.tsx"
                                )
                );


        assertThat(
                page
        )
                .contains(
                        "Executive command center",
                        "Bid portfolio pipeline",
                        "Procurement market intelligence",
                        "Operational alert center",
                        "Procurement data operations",
                        "Architecture & trust boundaries",
                        "Portfolio capture checklist"
                );
    }


    @Test
    void showcaseIsDiscoverableFromCentralNavigationAndDashboard()
            throws Exception {

        Path root =
                repositoryRoot();


        String navigation =
                Files.readString(
                        root.resolve(
                                "frontend/src/features/navigation/"
                                        +
                                        "product-navigation.ts"
                        )
                );


        String dashboard =
                Files.readString(
                        root.resolve(
                                "frontend/src/app/(workspace)/"
                                        +
                                        "dashboard/page.tsx"
                        )
                );


        assertThat(
                navigation
        )
                .contains(
                        "\"/showcase\"",
                        "Portfolio showcase"
                );


        assertThat(
                dashboard
        )
                .contains(
                        "href=\"/showcase\"",
                        "Portfolio showcase"
                );
    }


    @Test
    void releaseGateCoversShowcaseAndCurrentVersion()
            throws Exception {

        String releaseGate =
                Files.readString(
                        repositoryRoot()
                                .resolve(
                                        "scripts/release-gate.sh"
                                )
                );


        assertThat(
                releaseGate
        )
                .contains(
                        "1.0.0",
                        "\"/showcase\"",
                        "showcase/page.tsx"
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
