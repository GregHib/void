#!/usr/bin/env python3
"""
vendor-manifest.py

Purpose:
  Create a `sha256sum`-compatible manifest for vendored jars so offline builds
  can verify integrity.

How it works:
  - Reads `vendor/maven.classpath` (or `--vendor-classpath`) to get the list of
    vendored jar paths.
  - Computes SHA-256 for each jar and writes `SHA256SUMS` lines of the form:
      <digest>  <relative-path>
"""
from __future__ import annotations

import argparse
import hashlib
import sys
from pathlib import Path


def sha256(path: Path) -> str:
    hasher = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            hasher.update(chunk)
    return hasher.hexdigest()


def main() -> int:
    repo = Path(__file__).resolve().parents[1]

    parser = argparse.ArgumentParser(description="Write SHA256SUMS for vendored jars.")
    parser.add_argument("--vendor-classpath", default="vendor/maven.classpath")
    parser.add_argument("--out", default="vendor/SHA256SUMS")
    args = parser.parse_args()

    vendor_cp = Path(args.vendor_classpath)
    if not vendor_cp.is_absolute():
        vendor_cp = repo / vendor_cp
    if not vendor_cp.exists():
        print(f"[vendor-manifest] ERROR: missing {vendor_cp}", file=sys.stderr)
        return 2

    out = Path(args.out)
    if not out.is_absolute():
        out = repo / out
    out.parent.mkdir(parents=True, exist_ok=True)

    raw = vendor_cp.read_text(encoding="utf-8").strip()
    if not raw:
        print(f"[vendor-manifest] ERROR: empty classpath in {vendor_cp}", file=sys.stderr)
        return 2

    jar_paths = [Path(p) for p in raw.split(":") if p]
    resolved: list[Path] = []
    for p in jar_paths:
        resolved.append(p if p.is_absolute() else (repo / p))

    missing = [p for p in resolved if not p.exists()]
    if missing:
        print("[vendor-manifest] ERROR: missing vendored jars:", file=sys.stderr)
        for p in missing[:20]:
            print(f"  {p}", file=sys.stderr)
        if len(missing) > 20:
            print(f"  ... and {len(missing) - 20} more", file=sys.stderr)
        return 2

    lines = []
    for p in resolved:
        digest = sha256(p)
        rel = p.relative_to(repo).as_posix()
        lines.append(f"{digest}  {rel}")

    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"[vendor-manifest] Wrote {out} ({len(lines)} entries)", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
