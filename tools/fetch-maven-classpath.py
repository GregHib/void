#!/usr/bin/env python3
"""
fetch-maven-classpath.py

Purpose:
  Fetch Maven dependencies using `coursier` and output a single combined
  `:`-separated classpath (on stdout).

How it works:
  - Reads a list of Maven coordinates from `--coords` (one per line).
  - Invokes `cs fetch --classpath` in chunks (to keep command lines manageable).
  - Collects all jar paths into a set, then prints a sorted classpath.

Notes:
  - Respects `COURSIER_CACHE` (if set by the caller) for caching downloads.
  - This script only prints the classpath; callers typically redirect it into a
    file (e.g. `build/.../maven.classpath`).
"""
from __future__ import annotations

import argparse
import os
import subprocess
import sys
from pathlib import Path


def chunks(items: list[str], n: int) -> list[list[str]]:
    return [items[i : i + n] for i in range(0, len(items), n)]


def main() -> int:
    parser = argparse.ArgumentParser(description="Fetch Maven deps via coursier and output a combined classpath.")
    parser.add_argument("--cs", required=True, help="Path to coursier launcher (cs).")
    parser.add_argument("--coords", required=True, help="File containing Maven coordinates (one per line).")
    parser.add_argument("--chunk", type=int, default=40, help="How many coords per cs invocation.")
    args = parser.parse_args()

    cs = Path(args.cs)
    coords_file = Path(args.coords)
    if not cs.exists():
        print(f"[fetch-maven-classpath] ERROR: cs not found: {cs}", file=sys.stderr)
        return 2
    if not coords_file.exists():
        print(f"[fetch-maven-classpath] ERROR: coords file not found: {coords_file}", file=sys.stderr)
        return 2

    coords = [line.strip() for line in coords_file.read_text(encoding="utf-8").splitlines() if line.strip()]
    if not coords:
        print("", end="")
        return 0

    env = os.environ.copy()
    all_jars: set[str] = set()

    for group in chunks(coords, args.chunk):
        proc = subprocess.run(
            [str(cs), "fetch", "--classpath", *group],
            check=False,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            env=env,
        )
        if proc.returncode != 0:
            sys.stderr.write(proc.stderr)
            print(f"[fetch-maven-classpath] ERROR: cs fetch failed (exit {proc.returncode})", file=sys.stderr)
            return proc.returncode

        cp = proc.stdout.strip()
        if not cp:
            continue
        for part in cp.split(":"):
            if part:
                all_jars.add(part)

    print(":".join(sorted(all_jars)))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
