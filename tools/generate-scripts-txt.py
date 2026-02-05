#!/usr/bin/env python3
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


def strip_comments(text: str) -> str:
    # Good-enough stripping for script discovery; avoids matching commented-out classes.
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    text = re.sub(r"//.*?$", "", text, flags=re.MULTILINE)
    return text


def main() -> int:
    root = Path(__file__).resolve().parents[1]

    parser = argparse.ArgumentParser(description="Generate game scripts.txt (list of Script-implementing classes).")
    parser.add_argument(
        "--input",
        default=str(root / "game" / "src" / "main" / "kotlin" / "content"),
        help="Root directory containing Kotlin content scripts.",
    )
    parser.add_argument(
        "--output",
        default=str(root / "game" / "src" / "main" / "resources" / "scripts.txt"),
        help="Output scripts.txt path.",
    )
    args = parser.parse_args()

    input_dir = Path(args.input)
    output_file = Path(args.output)
    if not input_dir.exists():
        print(f"[generate-scripts-txt] ERROR: input dir not found: {input_dir}", file=sys.stderr)
        return 2

    scripts: set[str] = set()
    kotlin_files = sorted(input_dir.rglob("*.kt"))
    for file in kotlin_files:
        text = strip_comments(file.read_text(encoding="utf-8"))
        pkg_match = re.search(r"^\s*package\s+([A-Za-z0-9_.]+)\s*$", text, flags=re.MULTILINE)
        package = pkg_match.group(1) if pkg_match else ""

        for match in re.finditer(r"\bclass\s+([A-Za-z_][A-Za-z0-9_]*)\b", text):
            name = match.group(1)
            start = match.start()
            brace = text.find("{", match.end())
            header = text[start : (brace if brace != -1 else min(len(text), match.end() + 600))]
            if re.search(r":\s*Script\b", header):
                scripts.add(f"{package}.{name}" if package else name)

    output_file.parent.mkdir(parents=True, exist_ok=True)
    content = "\n".join(sorted(scripts)) + ("\n" if scripts else "")
    if output_file.exists():
        existing = output_file.read_text(encoding="utf-8")
        if existing == content:
            print(f"[generate-scripts-txt] Unchanged {output_file} ({len(scripts)} scripts)", file=sys.stderr)
            return 0

    output_file.write_text(content, encoding="utf-8")
    print(f"[generate-scripts-txt] Wrote {output_file} ({len(scripts)} scripts)", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
