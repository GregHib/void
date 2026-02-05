#!/usr/bin/env python3
from __future__ import annotations

import argparse
import hashlib
import os
import shutil
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

    parser = argparse.ArgumentParser(description="Vendor resolved Maven jar dependencies for offline builds.")
    parser.add_argument("--classpath-file", required=True, help="File containing ':'-separated jar classpath.")
    parser.add_argument("--out-dir", default="vendor/jars", help="Directory to write vendored jars into.")
    parser.add_argument("--out-classpath", default="vendor/maven.classpath", help="Path to write vendored classpath file.")
    args = parser.parse_args()

    classpath_file = Path(args.classpath_file)
    if not classpath_file.is_absolute():
        classpath_file = repo / classpath_file
    if not classpath_file.exists():
        print(f"[vendor-deps] ERROR: classpath file not found: {classpath_file}", file=sys.stderr)
        return 2

    out_dir = Path(args.out_dir)
    if not out_dir.is_absolute():
        out_dir = repo / out_dir
    out_dir.mkdir(parents=True, exist_ok=True)

    out_classpath = Path(args.out_classpath)
    if not out_classpath.is_absolute():
        out_classpath = repo / out_classpath
    out_classpath.parent.mkdir(parents=True, exist_ok=True)

    raw = classpath_file.read_text(encoding="utf-8").strip()
    if not raw:
        print("[vendor-deps] ERROR: empty classpath file", file=sys.stderr)
        return 2

    jar_paths = [Path(p) for p in raw.split(":") if p]
    missing = [p for p in jar_paths if not p.exists()]
    if missing:
        print("[vendor-deps] ERROR: missing jars from classpath:", file=sys.stderr)
        for p in missing[:20]:
            print(f"  {p}", file=sys.stderr)
        if len(missing) > 20:
            print(f"  ... and {len(missing) - 20} more", file=sys.stderr)
        return 2

    # Preserve original classpath ordering, but de-duplicate by content hash.
    seen_hashes: set[str] = set()
    vendored_paths: list[Path] = []

    for jar in jar_paths:
        digest = sha256(jar)
        if digest in seen_hashes:
            continue
        seen_hashes.add(digest)

        # Keep original basename for readability, but key by hash to avoid collisions.
        safe_name = jar.name.replace(os.sep, "_")
        dest = out_dir / f"{digest[:16]}-{safe_name}"

        if dest.exists():
            # Assume it is already correct; avoid re-hashing dest for speed.
            vendored_paths.append(dest)
            continue

        shutil.copy2(jar, dest)
        vendored_paths.append(dest)

    rel_paths = []
    for p in vendored_paths:
        try:
            rel_paths.append(p.relative_to(repo).as_posix())
        except ValueError:
            rel_paths.append(p.as_posix())

    out_classpath.write_text(":".join(rel_paths), encoding="utf-8")
    print(f"[vendor-deps] Vendored {len(vendored_paths)} jars to {out_dir}", file=sys.stderr)
    print(f"[vendor-deps] Wrote {out_classpath}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

