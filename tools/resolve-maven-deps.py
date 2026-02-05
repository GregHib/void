#!/usr/bin/env python3
"""
resolve-maven-deps.py

Purpose:
  Emit a list of Maven coordinates needed to compile/run the server outside
  Gradle (for the Ninja + `kotlinc` build).

How it works:
  - Loads versions and library coordinates from `gradle/libs.versions.toml`.
  - Scans each module’s `build.gradle.kts` for `implementation(libs.<...>)` and
    `implementation(libs.bundles.<...>)` usage.
  - Expands bundles and resolves version refs from the catalog.
  - Normalizes selected Kotlin Multiplatform deps to explicit JVM artifacts
    (coursier does not perform Gradle’s variant resolution).

Output:
  Writes `group:artifact:version` coordinates to stdout (one per line).
"""
from __future__ import annotations

import argparse
import re
import sys
from pathlib import Path


def load_toml(path: Path) -> dict:
    import tomllib

    return tomllib.loads(path.read_text(encoding="utf-8"))


def main() -> int:
    root = Path(__file__).resolve().parents[1]
    toml = load_toml(root / "gradle" / "libs.versions.toml")

    versions: dict[str, str] = toml.get("versions", {})
    libraries: dict[str, dict] = toml.get("libraries", {})
    bundles: dict[str, list[str]] = toml.get("bundles", {})

    # Gradle can select platform-specific variants for Kotlin Multiplatform (KMP) dependencies.
    # When building outside Gradle (coursier + kotlinc), we need the JVM artifacts explicitly.
    force_jvm_artifact = {
        "net.pearx.kasechange:kasechange": "net.pearx.kasechange:kasechange-jvm",
        "org.jetbrains.kotlinx:kotlinx-coroutines-core": "org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm",
        "io.insert-koin:koin-core": "io.insert-koin:koin-core-jvm",
    }

    def normalize_module(module: str) -> str:
        return force_jvm_artifact.get(module, module)

    def normalize_coord(coord: str) -> str:
        # group:artifact:version
        parts = coord.split(":")
        if len(parts) != 3:
            return coord
        module = normalize_module(f"{parts[0]}:{parts[1]}")
        group, artifact = module.split(":", 1)
        return f"{group}:{artifact}:{parts[2]}"

    def coord_for_library_key(key: str) -> str:
        entry = libraries.get(key)
        if not entry:
            raise KeyError(key)
        module = normalize_module(entry["module"])
        version = entry.get("version")
        if isinstance(version, dict) and "ref" in version:
            version = versions[version["ref"]]
        elif isinstance(version, str):
            pass
        else:
            version = None
        if not version:
            raise RuntimeError(f"Library '{key}' missing version/version.ref")
        return f"{module}:{version}"

    accessor_to_library_key: dict[str, str] = {}
    for key in libraries.keys():
        accessor_to_library_key[key.replace("-", ".")] = key

    bundle_accessor_to_key: dict[str, str] = {}
    for key in bundles.keys():
        bundle_accessor_to_key[key.replace("-", ".")] = key

    parser = argparse.ArgumentParser(description="Resolve Maven coordinates from Gradle Kotlin DSL + version catalog.")
    parser.add_argument(
        "--include-db",
        action="store_true",
        help="Include :database runtime dependencies (server with database storage).",
    )
    parser.add_argument(
        "--include-tools",
        action="store_true",
        help="Include :tools dependencies (heavy; not needed for server).",
    )
    args = parser.parse_args()

    modules = ["buffer", "types", "config", "cache", "network", "engine", "game"]
    if args.include_db:
        modules.insert(modules.index("game"), "database")
    if args.include_tools:
        modules.append("tools")

    dep_lib_re = re.compile(r'\b(?:implementation|api|compileOnly|runtimeOnly)\(\s*libs\.([A-Za-z0-9_.]+)\s*\)')
    dep_bundle_re = re.compile(
        r'\b(?:implementation|api|compileOnly|runtimeOnly)\(\s*libs\.bundles\.([A-Za-z0-9_.]+)\s*\)'
    )
    dep_string_re = re.compile(r'\b(?:implementation|api|compileOnly|runtimeOnly)\(\s*"([^"]+)"\s*\)')

    coords: set[str] = set()
    warnings: list[str] = []

    for module in modules:
        build_file = root / module / "build.gradle.kts"
        if not build_file.exists():
            warnings.append(f"Missing build file: {build_file}")
            continue
        text = build_file.read_text(encoding="utf-8")

        for accessor in dep_lib_re.findall(text):
            # Ignore kotlin("...") helpers; those are handled via Kotlin distribution libs in the Make-based build.
            if accessor.startswith("kotlin"):
                continue
            if accessor.startswith("bundles."):
                continue
            key = accessor_to_library_key.get(accessor)
            if not key:
                warnings.append(f"Unknown version-catalog library accessor 'libs.{accessor}' in {build_file}")
                continue
            coords.add(coord_for_library_key(key))

        for accessor in dep_bundle_re.findall(text):
            bundle_key = bundle_accessor_to_key.get(accessor)
            if not bundle_key:
                warnings.append(f"Unknown version-catalog bundle accessor 'libs.bundles.{accessor}' in {build_file}")
                continue
            for lib_key in bundles[bundle_key]:
                coords.add(coord_for_library_key(lib_key))

        for literal in dep_string_re.findall(text):
            coords.add(normalize_coord(literal))

    for w in warnings:
        print(f"[resolve-maven-deps] WARN: {w}", file=sys.stderr)

    for c in sorted(coords):
        print(c)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
