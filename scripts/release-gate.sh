#!/usr/bin/env bash
set -euo pipefail

###############################################################################
# TENDERPILOT LOCAL RELEASE GATE
#
# Free/local only.
# No CI/CD.
# No network dependency.
#
# Normal release usage:
#
#   ./scripts/release-gate.sh
#
# During an uncommitted development step:
#
#   TENDERPILOT_RELEASE_ALLOW_DIRTY=1 ./scripts/release-gate.sh
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

ALLOW_DIRTY="${TENDERPILOT_RELEASE_ALLOW_DIRTY:-0}"

RELEASE_LOG_DIR="$(
  mktemp -d \
    "${TMPDIR:-/tmp}/tenderpilot-release-gate.XXXXXX"
)"

STATIC_LOG="${RELEASE_LOG_DIR}/01-static.log"
QUALITY_LOG="${RELEASE_LOG_DIR}/02-quality.log"
DIFF_LOG="${RELEASE_LOG_DIR}/03-diff.log"

RESULT_FILE="${RELEASE_LOG_DIR}/TENDERPILOT_RELEASE_GATE_RESULT.txt"

###############################################################################
# HELPERS
###############################################################################

fail() {
  echo "[FAIL] $*"
  exit 1
}

###############################################################################
# REQUIRED COMMANDS
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
# CLEANLINESS
###############################################################################

if [[ "${ALLOW_DIRTY}" != "1" ]] && \
   [[ -n "$(git status --porcelain)" ]]; then

  echo "[FAIL] RELEASE_WORKTREE_NOT_CLEAN"

  git status --short

  exit 1

fi

###############################################################################
# STATIC RELEASE CONTRACT
###############################################################################

python3 <<'PY' 2>&1 | tee "${STATIC_LOG}"
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET


root = Path.cwd()


###############################################################################
# VERSION SYNCHRONIZATION
###############################################################################

pom_path = root / "backend/pom.xml"

pom_text = pom_path.read_text(
    encoding="utf-8"
)


application_text = (
    root
    /
    "backend/src/main/resources/application.yml"
).read_text(
    encoding="utf-8"
)


pom_root = ET.parse(
    pom_path
).getroot()


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


if version_node is None:

    raise SystemExit(
        "Unable to determine Maven project version."
    )


version = (
    version_node.text
    or
    ""
).strip()


if version != "0.24.0-SNAPSHOT":

    raise SystemExit(
        f"Unexpected Maven version: {version}"
    )


if f"version: {version}" not in application_text:

    raise SystemExit(
        "application.yml version does not match pom.xml"
    )


###############################################################################
# MIGRATION REGISTRATION
###############################################################################

changes_dir = (
    root
    /
    "backend/src/main/resources/db/changelog/changes"
)


master = (
    root
    /
    "backend/src/main/resources/db/changelog/db.changelog-master.yaml"
).read_text(
    encoding="utf-8"
)


change_files = sorted(
    path
    for path in changes_dir.glob(
        "*.yaml"
    )
    if path.is_file()
)


if not change_files:

    raise SystemExit(
        "No Liquibase change files found."
    )


numeric_prefixes = []


for path in change_files:

    filename = path.name


    count = master.count(
        filename
    )


    if count != 1:

        raise SystemExit(
            f"Liquibase registration count for {filename}: {count}"
        )


    match = re.match(
        r"^(\d+)-",
        filename
    )


    if match:

        numeric_prefixes.append(
            int(
                match.group(
                    1
                )
            )
        )


if len(
    numeric_prefixes
) != len(
    set(
        numeric_prefixes
    )
):

    raise SystemExit(
        "Duplicate numeric Liquibase migration prefix detected."
    )


if max(
    numeric_prefixes,
    default=0
) != 15:

    raise SystemExit(
        "Unexpected latest migration number; Step 24 expects 015."
    )


###############################################################################
# FRONTEND PRODUCT ROUTES
###############################################################################

routes = {

    "/workspace":
        "frontend/src/app/(workspace)/workspace/page.tsx",

    "/dashboard":
        "frontend/src/app/(workspace)/dashboard/page.tsx",

    "/notifications":
        "frontend/src/app/(workspace)/notifications/page.tsx",

    "/opportunities":
        "frontend/src/app/(workspace)/opportunities/page.tsx",

    "/matches":
        "frontend/src/app/(workspace)/matches/page.tsx",

    "/bid-decisions":
        "frontend/src/app/(workspace)/bid-decisions/page.tsx",

    "/market":
        "frontend/src/app/(workspace)/market/page.tsx",

    "/buyers":
        "frontend/src/app/(workspace)/buyers/page.tsx",

    "/suppliers":
        "frontend/src/app/(workspace)/suppliers/page.tsx",

    "/intelligence":
        "frontend/src/app/(workspace)/intelligence/page.tsx",

    "/data-operations":
        "frontend/src/app/(workspace)/data-operations/page.tsx",

}


registry = (
    root
    /
    "frontend/src/features/navigation/product-navigation.ts"
).read_text(
    encoding="utf-8"
)


for route, raw_path in routes.items():

    if f'"{route}"' not in registry:

        raise SystemExit(
            f"Navigation registry missing {route}"
        )


    if not (
        root
        /
        raw_path
    ).is_file():

        raise SystemExit(
            f"Frontend route page missing: {route}"
        )


###############################################################################
# WORKSPACE TEMPLATE / GLOBAL COMMAND PALETTE
###############################################################################

template = (
    root
    /
    "frontend/src/app/(workspace)/template.tsx"
).read_text(
    encoding="utf-8"
)


experience = (
    root
    /
    "frontend/src/features/navigation/product-experience-layer.tsx"
).read_text(
    encoding="utf-8"
)


for required in [
    "ProductExperienceLayer",
    "<ProductExperienceLayer />",
]:

    if required not in template:

        raise SystemExit(
            f"Workspace template contract missing: {required}"
        )


for required in [
    "metaKey",
    "ctrlKey",
    "Escape",
    "Search TenderPilot",
]:

    if required not in experience:

        raise SystemExit(
            f"Command-palette contract missing: {required}"
        )


###############################################################################
# CRITICAL API SURFACES
###############################################################################

java_root = (
    root
    /
    "backend/src/main/java"
)


java_text = "\n".join(
    path.read_text(
        encoding="utf-8"
    )
    for path in java_root.rglob(
        "*.java"
    )
)


for required in [
    '"/api/v1/dashboard"',
    '"/api/v1/notifications"',
    '"/api/v1/data-operations"',
    '"/api/v1/market"',
    '"/api/v1/buyers"',
    '"/api/v1/suppliers"',
    '"/api/v1/matches"',
    '"/api/v1/bid-decisions"',
    '"/api/v1/bid-workspaces"',
]:

    if required not in java_text:

        raise SystemExit(
            f"Critical API surface missing: {required}"
        )


###############################################################################
# PRIVATE DATA MUST NOT ENTER GLOBAL SEARCH
###############################################################################

search_root = (
    root
    /
    "backend/src/main/java/com/tenderpilot/search"
)


if search_root.is_dir():

    search_text = "\n".join(
        path.read_text(
            encoding="utf-8"
        )
        for path in search_root.rglob(
            "*.java"
        )
    )


    for forbidden in [
        "com.tenderpilot.dashboard",
        "com.tenderpilot.notification",
        "organization_notifications",
        "organization_watchlist_items",
        "organization_bid_documents",
        "organization_bid_rag_chunks",
        "organization_bid_compliance_reviews",
        "organization_bid_tasks",
    ]:

        if forbidden in search_text:

            raise SystemExit(
                f"Private data leaked to global search: {forbidden}"
            )


###############################################################################
# PUBLIC MARKET MUST REMAIN PRIVATE-DATA FREE
###############################################################################

market_root = (
    root
    /
    "backend/src/main/java/com/tenderpilot/marketanalytics"
)


market_text = "\n".join(
    path.read_text(
        encoding="utf-8"
    )
    for path in market_root.rglob(
        "*.java"
    )
)


for forbidden in [
    "CurrentOrganizationResolver",
    "organizationId",
    "com.tenderpilot.company",
    "com.tenderpilot.bidworkspace",
    "com.tenderpilot.biddecision",
    "com.tenderpilot.compliance",
    "com.tenderpilot.localai",
    "com.tenderpilot.rag",
    "com.tenderpilot.bidplanning",
    "com.tenderpilot.notification",
    "com.tenderpilot.dashboard",
]:

    if forbidden in market_text:

        raise SystemExit(
            f"Private dependency in public market analytics: {forbidden}"
        )


###############################################################################
# NO OBVIOUS TRACKED SECRETS
###############################################################################

tracked = subprocess.run(
    [
        "git",
        "ls-files",
    ],
    check=True,
    text=True,
    capture_output=True,
).stdout.splitlines()


secret_patterns = [

    re.compile(
        r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"
    ),

    re.compile(
        r"\bAKIA[0-9A-Z]{16}\b"
    ),

    re.compile(
        r"\bghp_[A-Za-z0-9]{30,}\b"
    ),

    re.compile(
        r"\bxox[baprs]-[A-Za-z0-9-]{20,}\b"
    ),

]


for raw_path in tracked:

    path = root / raw_path


    if not path.is_file():

        continue


    if any(
        part in {
            "target",
            "node_modules",
            ".next",
        }
        for part in path.parts
    ):

        continue


    try:

        text = path.read_text(
            encoding="utf-8"
        )

    except UnicodeDecodeError:

        continue


    for pattern in secret_patterns:

        if pattern.search(
            text
        ):

            raise SystemExit(
                f"Potential tracked secret detected in {raw_path}"
            )


###############################################################################
# NO GENERATED BUILD DIRECTORIES TRACKED
###############################################################################

for raw_path in tracked:

    normalized = raw_path.replace(
        "\\",
        "/"
    )


    if (
        "/node_modules/" in f"/{normalized}/"
        or
        "/.next/" in f"/{normalized}/"
        or
        "/target/" in f"/{normalized}/"
    ):

        raise SystemExit(
            f"Generated build output is tracked: {raw_path}"
        )


###############################################################################
# RELEASE TESTS PRESENT
###############################################################################

for required in [
    "backend/src/test/java/com/tenderpilot/release/"
    "ReleaseProductSurfaceTest.java",

    "backend/src/test/java/com/tenderpilot/release/"
    "ReleaseSecurityBoundaryTest.java",
]:

    if not (
        root
        /
        required
    ).is_file():

        raise SystemExit(
            f"Release test missing: {required}"
        )


print(
    f"RELEASE_VERSION={version}"
)

print(
    "VERSION_SYNCHRONIZATION=PASS"
)

print(
    "LIQUIBASE_REGISTRATION=PASS"
)

print(
    "LATEST_LIQUIBASE_MIGRATION=015"
)

print(
    "PRIMARY_FRONTEND_ROUTES=PASS"
)

print(
    "GLOBAL_PRODUCT_NAVIGATION=PASS"
)

print(
    "CRITICAL_API_SURFACES=PASS"
)

print(
    "PRIVATE_DATA_SEARCH_BOUNDARY=PASS"
)

print(
    "PUBLIC_MARKET_PRIVATE_DATA_BOUNDARY=PASS"
)

print(
    "TRACKED_SECRET_HEURISTIC_SCAN=PASS"
)

print(
    "TRACKED_BUILD_OUTPUT_SCAN=PASS"
)

print(
    "RELEASE_STATIC_CONTRACT=PASS"
)
PY

###############################################################################
# COMPLETE EXISTING QUALITY GATE
###############################################################################

set +e

./scripts/check.sh \
  2>&1 | tee "${QUALITY_LOG}"

STATUS="${PIPESTATUS[0]}"

set -e


if [[ "${STATUS}" -ne 0 ]]; then

  echo "[FAIL] EXISTING_QUALITY_GATE"
  exit "${STATUS}"

fi


for required in \
  'SHELL_SCRIPT_SYNTAX=PASS' \
  'BACKEND_TESTS=PASS' \
  'FRONTEND_LINT=PASS' \
  'FRONTEND_TYPECHECK=PASS' \
  'FRONTEND_BUILD=PASS' \
  'COMPOSE_VALIDATION=PASS' \
  'QUALITY_GATE_STATUS=PASS'
do

  grep -Fq \
    "${required}" \
    "${QUALITY_LOG}" || {
      echo "[FAIL] QUALITY_GATE_CONTRACT_MISSING=${required}"
      exit 1
    }

done

###############################################################################
# AUTHORITATIVE TEST COUNT
###############################################################################

BACKEND_TEST_TOTAL="$(
  python3 <<'PY'
from pathlib import Path
import xml.etree.ElementTree as ET


reports = Path(
    "backend/target/surefire-reports"
)


tests = 0
failures = 0
errors = 0
files = 0


for path in reports.glob(
    "TEST-*.xml"
):

    files += 1

    root = ET.parse(
        path
    ).getroot()

    tests += int(
        root.attrib.get(
            "tests",
            "0",
        )
    )

    failures += int(
        root.attrib.get(
            "failures",
            "0",
        )
    )

    errors += int(
        root.attrib.get(
            "errors",
            "0",
        )
    )


if files == 0:

    raise SystemExit(
        "No Surefire reports found."
    )


if failures != 0 or errors != 0:

    raise SystemExit(
        f"failures={failures} errors={errors}"
    )


print(
    tests
)
PY
)"


if [[ "${BACKEND_TEST_TOTAL}" -lt 207 ]]; then

  echo "[FAIL] RELEASE_BACKEND_TEST_TOTAL_TOO_LOW=${BACKEND_TEST_TOTAL}"
  echo "MINIMUM_EXPECTED=207"

  exit 1

fi

###############################################################################
# DIFF / CLEANLINESS
###############################################################################

set +e

git diff \
  --check \
  2>&1 | tee "${DIFF_LOG}"

DIFF_STATUS="${PIPESTATUS[0]}"

set -e


if [[ "${DIFF_STATUS}" -ne 0 ]]; then

  echo "[FAIL] RELEASE_DIFF_CHECK"
  exit "${DIFF_STATUS}"

fi


if [[ "${ALLOW_DIRTY}" != "1" ]] && \
   [[ -n "$(git status --porcelain)" ]]; then

  echo "[FAIL] RELEASE_WORKTREE_NOT_CLEAN_AFTER_GATE"

  git status --short

  exit 1

fi

###############################################################################
# RESULT
###############################################################################

{
  echo
  echo "================================================================================"
  echo "TENDERPILOT_RELEASE_GATE_RESULT"
  echo "================================================================================"
  echo

  echo "RELEASE_VERSION=0.24.0-SNAPSHOT"
  echo

  echo "RELEASE_STATIC_CONTRACT=PASS"
  echo "VERSION_SYNCHRONIZATION=PASS"
  echo "LIQUIBASE_REGISTRATION=PASS"
  echo "PRIMARY_FRONTEND_ROUTES=PASS"
  echo "GLOBAL_PRODUCT_NAVIGATION=PASS"
  echo "CRITICAL_API_SURFACES=PASS"
  echo
  echo "PRIVATE_DATA_SEARCH_BOUNDARY=PASS"
  echo "PUBLIC_MARKET_PRIVATE_DATA_BOUNDARY=PASS"
  echo
  echo "TRACKED_SECRET_HEURISTIC_SCAN=PASS"
  echo "TRACKED_BUILD_OUTPUT_SCAN=PASS"
  echo
  echo "BACKEND_TESTS=${BACKEND_TEST_TOTAL}/${BACKEND_TEST_TOTAL}_PASS"
  echo "FRONTEND_LINT=PASS"
  echo "FRONTEND_TYPECHECK=PASS"
  echo "FRONTEND_BUILD=PASS"
  echo "COMPOSE_VALIDATION=PASS"
  echo
  echo "EXISTING_PROJECT_QUALITY_GATE=PASS"
  echo "RELEASE_DIFF_CHECK=PASS"

  if [[ "${ALLOW_DIRTY}" == "1" ]]; then
    echo "RELEASE_WORKTREE_MODE=DIRTY_ALLOWED_FOR_DEVELOPMENT_STEP"
  else
    echo "RELEASE_WORKTREE_MODE=CLEAN_REQUIRED"
    echo "WORKTREE_CLEAN=YES"
  fi

  echo
  echo "RELEASE_GATE_STATUS=PASS"
  echo
  echo "LOG_DIR=${RELEASE_LOG_DIR}"
  echo "================================================================================"

} | tee "${RESULT_FILE}"


if command -v pbcopy >/dev/null 2>&1; then

  pbcopy < "${RESULT_FILE}"

  echo
  echo "[PASS] RELEASE_GATE_RESULT_COPIED_TO_CLIPBOARD"

fi
