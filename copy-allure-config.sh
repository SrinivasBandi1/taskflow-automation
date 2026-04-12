#!/usr/bin/env bash
# copy-allure-config.sh
# Called by CI before generating the Allure report.
# Copies categories.json into the allure-results directory so Allure
# picks it up when building the report.

set -e

RESULTS_DIR="target/allure-results"
CATEGORIES_SRC="src/test/resources/categories.json"

mkdir -p "${RESULTS_DIR}"

if [ -f "${CATEGORIES_SRC}" ]; then
  cp "${CATEGORIES_SRC}" "${RESULTS_DIR}/categories.json"
  echo "✅ Copied categories.json to ${RESULTS_DIR}"
else
  echo "⚠️  categories.json not found at ${CATEGORIES_SRC} — skipping"
fi
