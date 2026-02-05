#!/usr/bin/env bash
# bootstrap-cache.sh
#
# Purpose:
#   Ensure the RuneScape cache exists at `./data/cache/` for running the server
#   (in particular `data/cache/main_file_cache.dat2`).
#
# How it works:
#   - If the sentinel file already exists, it does nothing.
#   - Otherwise, it tries to populate `./data/cache/` by:
#       1) promoting an accidentally nested extraction under `data/cache/*/`
#       2) using `CACHE_DIR` (already-extracted cache directory)
#       3) extracting `CACHE_ZIP` (supports `.zip` and `.7z`)
#       4) downloading `CACHE_URL` (expects a direct `.zip`)
#     plus a small auto-detection path when no vars are provided.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DEST_DIR="$ROOT_DIR/data/cache"
SENTINEL="$DEST_DIR/main_file_cache.dat2"

usage() {
  cat <<'EOF'
Usage:
  CACHE_ZIP=/path/to/cache.zip bash tools/bootstrap-cache.sh
  CACHE_DIR=/path/to/extracted/cache bash tools/bootstrap-cache.sh
  CACHE_URL=https://example.com/cache.zip bash tools/bootstrap-cache.sh

Purpose:
  Ensure ./data/cache contains the RuneScape cache files (e.g. main_file_cache.dat2).

Notes:
  - Prefer CACHE_ZIP (local file) or CACHE_DIR (already extracted).
  - If no vars are provided, the script will also auto-pick the newest ./cache/*.7z (requires `7z`).
  - CACHE_URL is best-effort; some hosts (e.g. Mega) may not work in restricted environments.
EOF
}

if [[ -s "$SENTINEL" ]]; then
  echo "Cache already present at $DEST_DIR"
  exit 0
fi

mkdir -p "$DEST_DIR"

copy_cache_dir() {
  local src_dir="$1"
  if [[ ! -d "$src_dir" ]]; then
    echo "[bootstrap-cache] ERROR: not a directory: $src_dir" >&2
    exit 2
  fi
  if [[ ! -s "$src_dir/main_file_cache.dat2" ]]; then
    echo "[bootstrap-cache] ERROR: main_file_cache.dat2 not found in: $src_dir" >&2
    exit 2
  fi
  cp -a "$src_dir"/. "$DEST_DIR"/
}

promote_existing_nested_cache() {
  local nested
  nested="$(find "$DEST_DIR" -mindepth 2 -type f -name 'main_file_cache.dat2' -print -quit 2>/dev/null || true)"
  if [[ -n "$nested" ]]; then
    local src
    src="$(dirname "$nested")"
    echo "Found nested cache at $src; copying into $DEST_DIR"
    copy_cache_dir "$src"
    return 0
  fi
  return 1
}

extract_zip_then_copy() {
  local zip="$1"
  if [[ ! -f "$zip" ]]; then
    echo "[bootstrap-cache] ERROR: zip not found: $zip" >&2
    exit 2
  fi
  local tmp
  tmp="$(mktemp -d)"
  trap 'rm -rf "$tmp"' EXIT
  unzip -q "$zip" -d "$tmp"
  local dat2
  dat2="$(find "$tmp" -type f -name 'main_file_cache.dat2' -print -quit || true)"
  if [[ -z "$dat2" ]]; then
    echo "[bootstrap-cache] ERROR: main_file_cache.dat2 not found inside zip: $zip" >&2
    exit 2
  fi
  copy_cache_dir "$(dirname "$dat2")"
}

extract_7z_then_copy() {
  local archive="$1"
  if [[ ! -f "$archive" ]]; then
    echo "[bootstrap-cache] ERROR: archive not found: $archive" >&2
    exit 2
  fi
  if ! command -v 7z >/dev/null 2>&1; then
    echo "[bootstrap-cache] ERROR: 7z not installed but cache archive is .7z: $archive" >&2
    exit 2
  fi
  local tmp
  tmp="$(mktemp -d)"
  trap 'rm -rf "$tmp"' EXIT
  7z x -y -o"$tmp" "$archive" >/dev/null
  local dat2
  dat2="$(find "$tmp" -type f -name 'main_file_cache.dat2' -print -quit || true)"
  if [[ -z "$dat2" ]]; then
    echo "[bootstrap-cache] ERROR: main_file_cache.dat2 not found inside 7z: $archive" >&2
    exit 2
  fi
  copy_cache_dir "$(dirname "$dat2")"
}

# If the cache was extracted into a nested folder under ./data/cache/, promote it automatically.
if promote_existing_nested_cache; then
  if [[ -s "$SENTINEL" ]]; then
    echo "Cache bootstrapped into $DEST_DIR"
    exit 0
  fi
fi

# Auto-detect common local zip locations if not provided.
if [[ -z "${CACHE_DIR:-}" && -z "${CACHE_ZIP:-}" && -z "${CACHE_URL:-}" ]]; then
  if [[ -f "$ROOT_DIR/data/cache.zip" ]]; then
    CACHE_ZIP="$ROOT_DIR/data/cache.zip"
  elif [[ -f "$ROOT_DIR/data/cache/cache.zip" ]]; then
    CACHE_ZIP="$ROOT_DIR/data/cache/cache.zip"
  elif compgen -G "$ROOT_DIR/cache/*.7z" >/dev/null; then
    # Prefer newest matching archive in ./cache/
    CACHE_ZIP="$(ls -1t "$ROOT_DIR"/cache/*.7z | head -n 1)"
  fi
fi

if [[ -n "${CACHE_DIR:-}" ]]; then
  copy_cache_dir "$CACHE_DIR"
elif [[ -n "${CACHE_ZIP:-}" ]]; then
  case "$CACHE_ZIP" in
    *.7z) extract_7z_then_copy "$CACHE_ZIP" ;;
    *.zip) extract_zip_then_copy "$CACHE_ZIP" ;;
    *)
      echo "[bootstrap-cache] ERROR: unsupported cache archive type: $CACHE_ZIP" >&2
      echo "[bootstrap-cache] Supported: .zip, .7z (or provide CACHE_DIR)" >&2
      exit 2
      ;;
  esac
elif [[ -n "${CACHE_URL:-}" ]]; then
  archive="/tmp/void-cache.zip"
  echo "Downloading cache zip..."
  echo "  $CACHE_URL"
  if command -v curl >/dev/null 2>&1; then
    curl -fL --retry 3 --retry-delay 2 -o "$archive" "$CACHE_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$archive" "$CACHE_URL"
  else
    echo "[bootstrap-cache] ERROR: need curl or wget to download CACHE_URL" >&2
    exit 2
  fi
  extract_zip_then_copy "$archive"
else
  usage >&2
  echo "" >&2
  echo "[bootstrap-cache] ERROR: cache missing at $DEST_DIR and no CACHE_ZIP/CACHE_DIR/CACHE_URL provided." >&2
  exit 2
fi

if [[ ! -s "$SENTINEL" ]]; then
  echo "[bootstrap-cache] ERROR: cache bootstrap finished but $SENTINEL is still missing." >&2
  exit 2
fi

echo "Cache bootstrapped into $DEST_DIR"
