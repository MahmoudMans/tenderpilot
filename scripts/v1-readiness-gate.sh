#!/usr/bin/env bash
set -euo pipefail

###############################################################################
# TENDERPILOT — V1.0 READINESS GATE
#
# Final local release-certification authority.
#
# Normal final usage:
#
#   ./scripts/v1-readiness-gate.sh
#
# Development/pre-commit usage:
#
#   TENDERPILOT_V1_ALLOW_DIRTY=1 \
#   TENDERPILOT_V1_REQUIRE_TAG=0 \
#   ./scripts/v1-readiness-gate.sh
###############################################################################

SCRIPT_DIR="$(
  cd "$(
    dirname "${BASH_SOURCE[0]}"
  )" >/dev/null 2>&1
  pwd
)"

ROOT_DIR="$(
  cd "${SCRIPT_DIR}/.." >/dev/null 2>&1
  pwd
)"

cd "${ROOT_DIR}"

RELEASE_VERSION="1.0.0"
RELEASE_TAG="v1.0.0"
EXPECTED_LATEST_MIGRATION="015"
EXPECTED_MINIMUM_BACKEND_TESTS="222"

ALLOW_DIRTY="${TENDERPILOT_V1_ALLOW_DIRTY:-0}"
REQUIRE_TAG="${TENDERPILOT_V1_REQUIRE_TAG:-1}"

LOG_DIR="$(
  mktemp -d \
    "${TMPDIR:-/tmp}/tenderpilot-v1-readiness.XXXXXX"
)"

STATIC_LOG="${LOG_DIR}/01-static.log"
RELEASE_LOG="${LOG_DIR}/02-release-gate.log"
RESULT_FILE="${LOG_DIR}/TENDERPILOT_V1_READINESS_RESULT.txt"

###############################################################################
# HELPERS
###############################################################################

fail() {

  echo "[FAIL] $*"
  exit 1

}

###############################################################################
# COMMANDS
###############################################################################

for cmd in \
  git \
  java \
  mvn \
  node \
  npm \
  docker \
  python3
do

  command -v "${cmd}" >/dev/null 2>&1 || \
    fail "REQUIRED_COMMAND=${cmd}"

done

docker compose version >/dev/null 2>&1 || \
  fail "DOCKER_COMPOSE_V2_REQUIRED"

###############################################################################
# WORKTREE
###############################################################################

if [[ "${ALLOW_DIRTY}" != "1" ]] && \
   [[ -n "$(git status --porcelain)" ]]; then

  echo "[FAIL] V1_WORKTREE_NOT_CLEAN"

  git status --short

  exit 1

fi

###############################################################################
# STATIC V1 CERTIFICATION
###############################################################################

python3 <<'PY' 2>&1 | tee "${STATIC_LOG}"
from pathlib import Path
import json
import re
import xml.etree.ElementTree as ET


root = Path.cwd()


###############################################################################
# MAVEN PROJECT VERSION
###############################################################################

pom_path = (
    root
    /
    "backend/pom.xml"
)


tree = ET.parse(
    pom_path
)


pom_root = tree.getroot()


namespace = ""


if pom_root.tag.startswith("{"):

    namespace = (
        pom_root.tag.split(
            "}"
        )[0]
        +
        "}"
    )


version_node = pom_root.find(
    f"{namespace}version"
)


if (
    version_node is None
    or
    (
        version_node.text
        or
        ""
    ).strip()
    !=
    "1.0.0"
):

    raise SystemExit(
        "Maven project version is not 1.0.0."
    )


###############################################################################
# RUNTIME VERSION
###############################################################################

application = (
    root
    /
    "backend/src/main/resources/application.yml"
).read_text(
    encoding="utf-8"
)


if "version: 1.0.0" not in application:

    raise SystemExit(
        "Runtime application version is not 1.0.0."
    )


###############################################################################
# RELEASE MANIFEST
###############################################################################

manifest_path = (
    root
    /
    "release/tenderpilot-v1-release.json"
)


if not manifest_path.is_file():

    raise SystemExit(
        "V1 release manifest is missing."
    )


manifest = json.loads(
    manifest_path.read_text(
        encoding="utf-8"
    )
)


expected = {

    "product":
        "TenderPilot",

    "version":
        "1.0.0",

    "releaseTag":
        "v1.0.0",

    "releaseType":
        "stable",

    "featureFreezeBase":
        "60a2459",

    "latestLiquibaseMigration":
        "015",

    "minimumBackendTests":
        222,

    "releaseContractTests":
        26,

    "featureDevelopmentComplete":
        True,

    "productionBackendFeatureChange":
        False,

    "databaseMigrationChange":
        False,

    "syntheticShowcaseDataset":
        "tenderpilot-portfolio-showcase-v1",

    "showcaseRoute":
        "/showcase",

    "captureRoute":
        "/showcase/capture",

    "captureFrames":
        6,

    "captureResolution":
        "1440x900",

    "ciCdRequired":
        False,

    "paidServiceRequired":
        False,

}


for key, value in expected.items():

    if manifest.get(
        key
    ) != value:

        raise SystemExit(
            f"Release manifest mismatch: "
            f"{key}={manifest.get(key)!r}"
        )


###############################################################################
# MIGRATION FREEZE
###############################################################################

changes = (
    root
    /
    "backend/src/main/resources/db/changelog/changes"
)


prefix_pattern = re.compile(
    r"^(\d+)-"
)


numbers = []


for path in changes.glob(
    "*.yaml"
):

    match = prefix_pattern.match(
        path.name
    )


    if match:

        numbers.append(
            int(
                match.group(
                    1
                )
            )
        )


if not numbers:

    raise SystemExit(
        "No numbered Liquibase migrations found."
    )


if max(
    numbers
) != 15:

    raise SystemExit(
        f"Latest migration is {max(numbers)}, expected 15."
    )


###############################################################################
# SHOWCASE / CAPTURE FREEZE
###############################################################################

showcase_data = (
    root
    /
    "frontend/src/features/showcase/showcase-data.ts"
).read_text(
    encoding="utf-8"
)


capture = (
    root
    /
    "frontend/src/app/(capture)/showcase/capture/page.tsx"
).read_text(
    encoding="utf-8"
)


for required in [
    "SYNTHETIC_NON_PRODUCTION",
    "tenderpilot-portfolio-showcase-v1",
    "generatedFromLiveData:",
    "persistent:",
]:

    if required not in showcase_data:

        raise SystemExit(
            f"V1 showcase boundary missing: {required}"
        )


for required in [
    'data-capture-mode="portfolio"',
    'data-synthetic-data="true"',
    "CAPTURE_DATASET_ID",
]:

    if required not in capture:

        raise SystemExit(
            f"V1 capture contract missing: {required}"
        )


for forbidden in [
    "useAuth",
    "fetch(",
    "/api/v1/",
    "Date.now",
    "Math.random",
]:

    if forbidden in capture:

        raise SystemExit(
            f"V1 capture route has live dependency: {forbidden}"
        )


###############################################################################
# RELEASE GATE VERSION
###############################################################################

release_gate = (
    root
    /
    "scripts/release-gate.sh"
).read_text(
    encoding="utf-8"
)


for required in [
    "1.0.0",
    "-lt 222",
    "MINIMUM_EXPECTED=222",
    "RELEASE_GATE_STATUS=PASS",
]:

    if required not in release_gate:

        raise SystemExit(
            f"Release gate not frozen for V1: {required}"
        )


###############################################################################
# NO STALE PROJECT RELEASE VERSION IN AUTHORITATIVE RELEASE FILES
###############################################################################

authoritative = {

    "pom":
        pom_path.read_text(
            encoding="utf-8"
        ),

    "application":
        application,

    "release_gate":
        release_gate,

    "release_manifest":
        manifest_path.read_text(
            encoding="utf-8"
        ),

}


for name, text in authoritative.items():

    if "0.26.0-SNAPSHOT" in text:

        raise SystemExit(
            f"Stale development version remains in {name}"
        )


print(
    "V1_VERSION=1.0.0"
)

print(
    "V1_RELEASE_TYPE=STABLE"
)

print(
    "V1_RELEASE_MANIFEST=PASS"
)

print(
    "V1_MIGRATION_FREEZE=015"
)

print(
    "V1_SHOWCASE_SYNTHETIC_BOUNDARY=PASS"
)

print(
    "V1_CAPTURE_BOUNDARY=PASS"
)

print(
    "V1_RELEASE_GATE_CONFIGURATION=PASS"
)

print(
    "V1_STATIC_CERTIFICATION=PASS"
)
PY

###############################################################################
# COMPLETE RELEASE GATE
###############################################################################

set +e

TENDERPILOT_RELEASE_ALLOW_DIRTY="${ALLOW_DIRTY}" \
  ./scripts/release-gate.sh \
  2>&1 | tee "${RELEASE_LOG}"

STATUS="${PIPESTATUS[0]}"

set -e


if [[ "${STATUS}" -ne 0 ]]; then

  echo "[FAIL] V1_RELEASE_GATE"
  exit "${STATUS}"

fi


for required in \
  'RELEASE_STATIC_CONTRACT=PASS' \
  'VERSION_SYNCHRONIZATION=PASS' \
  'LIQUIBASE_REGISTRATION=PASS' \
  'PRIMARY_FRONTEND_ROUTES=PASS' \
  'GLOBAL_PRODUCT_NAVIGATION=PASS' \
  'CRITICAL_API_SURFACES=PASS' \
  'PRIVATE_DATA_SEARCH_BOUNDARY=PASS' \
  'PUBLIC_MARKET_PRIVATE_DATA_BOUNDARY=PASS' \
  'TRACKED_SECRET_HEURISTIC_SCAN=PASS' \
  'TRACKED_BUILD_OUTPUT_SCAN=PASS' \
  'FRONTEND_LINT=PASS' \
  'FRONTEND_TYPECHECK=PASS' \
  'FRONTEND_BUILD=PASS' \
  'COMPOSE_VALIDATION=PASS' \
  'EXISTING_PROJECT_QUALITY_GATE=PASS' \
  'RELEASE_DIFF_CHECK=PASS' \
  'RELEASE_GATE_STATUS=PASS'
do

  grep -Fq \
    "${required}" \
    "${RELEASE_LOG}" || {

      echo "[FAIL] V1_RELEASE_GATE_CONTRACT_MISSING=${required}"
      exit 1

    }

done


BACKEND_TEST_TOTAL="$(
  grep -Eo \
    'BACKEND_TESTS=[0-9]+/[0-9]+_PASS' \
    "${RELEASE_LOG}" \
    | tail -1 \
    | sed -E \
      's/BACKEND_TESTS=([0-9]+)\/.*/\1/'
)"


if [[ -z "${BACKEND_TEST_TOTAL}" ]]; then

  echo "[FAIL] V1_BACKEND_TEST_TOTAL_NOT_DISCOVERED"
  exit 1

fi


if [[ "${BACKEND_TEST_TOTAL}" -lt "${EXPECTED_MINIMUM_BACKEND_TESTS}" ]]; then

  echo "[FAIL] V1_BACKEND_TEST_TOTAL_TOO_LOW=${BACKEND_TEST_TOTAL}"
  echo "MINIMUM_EXPECTED=${EXPECTED_MINIMUM_BACKEND_TESTS}"

  exit 1

fi

###############################################################################
# OPTIONAL/FINAL TAG CERTIFICATION
###############################################################################

TAG_STATUS="NOT_REQUIRED"


if [[ "${REQUIRE_TAG}" == "1" ]]; then

  if ! git rev-parse \
    -q \
    --verify \
    "refs/tags/${RELEASE_TAG}" \
    >/dev/null 2>&1; then

    echo "[FAIL] V1_RELEASE_TAG_MISSING=${RELEASE_TAG}"
    exit 1

  fi


  TAG_COMMIT="$(
    git rev-parse \
      "${RELEASE_TAG}^{}"
  )"


  HEAD_COMMIT="$(
    git rev-parse \
      HEAD
  )"


  if [[ "${TAG_COMMIT}" != "${HEAD_COMMIT}" ]]; then

    echo "[FAIL] V1_RELEASE_TAG_DOES_NOT_POINT_TO_HEAD"
    echo "TAG_COMMIT=${TAG_COMMIT}"
    echo "HEAD_COMMIT=${HEAD_COMMIT}"

    exit 1

  fi


  TAG_STATUS="PASS"

fi

###############################################################################
# RESULT
###############################################################################

{
  echo
  echo "================================================================================"
  echo "TENDERPILOT_V1_READINESS_RESULT"
  echo "================================================================================"
  echo

  echo "V1_VERSION=${RELEASE_VERSION}"
  echo "V1_TAG=${RELEASE_TAG}"
  echo

  echo "V1_STATIC_CERTIFICATION=PASS"
  echo "V1_RELEASE_MANIFEST=PASS"
  echo "V1_MIGRATION_FREEZE=PASS"
  echo "V1_SHOWCASE_SYNTHETIC_BOUNDARY=PASS"
  echo "V1_CAPTURE_BOUNDARY=PASS"
  echo
  echo "BACKEND_TESTS=${BACKEND_TEST_TOTAL}/${BACKEND_TEST_TOTAL}_PASS"
  echo "FRONTEND_LINT=PASS"
  echo "FRONTEND_TYPECHECK=PASS"
  echo "FRONTEND_BUILD=PASS"
  echo "COMPOSE_VALIDATION=PASS"
  echo
  echo "LOCAL_RELEASE_GATE=PASS"
  echo "V1_TAG_CERTIFICATION=${TAG_STATUS}"
  echo

  if [[ "${ALLOW_DIRTY}" == "1" ]]; then
    echo "WORKTREE_MODE=DIRTY_ALLOWED_FOR_PRECOMMIT_CERTIFICATION"
  else
    echo "WORKTREE_MODE=CLEAN_REQUIRED"
    echo "WORKTREE_CLEAN=YES"
  fi

  echo
  echo "V1_READINESS_STATUS=PASS"
  echo
  echo "LOG_DIR=${LOG_DIR}"
  echo "================================================================================"

} | tee "${RESULT_FILE}"


if command -v pbcopy >/dev/null 2>&1; then

  pbcopy < "${RESULT_FILE}"

  echo
  echo "[PASS] V1_READINESS_RESULT_COPIED_TO_CLIPBOARD"

fi
