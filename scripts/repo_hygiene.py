#!/usr/bin/env python3
"""Small dependency-free repository hygiene check.

It catches obvious files that should not be committed. It is deliberately conservative
and is not a secret scanner or privacy audit.
"""
from __future__ import annotations

import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
FORBIDDEN_NAMES = {
    "local.properties",
    "secrets.properties",
    "google-services.json",
}
FORBIDDEN_SUFFIXES = {
    ".jks", ".keystore", ".p12", ".pfx", ".key",
}
FORBIDDEN_DIR_PARTS = {
    "raw", "participants", "private", "exports",
}
MAX_FILE_BYTES = 5 * 1024 * 1024
REQUIRED = {
    "README.md",
    "ROADMAP.md",
    "PRIVACY.md",
    "SECURITY.md",
    "CONTRIBUTING.md",
    "docs/PRODUCT_PRINCIPLES.md",
    "docs/MVP.md",
    "docs/DATA_MAP.md",
}


def tracked_candidates():
    for path in ROOT.rglob("*"):
        if not path.is_file():
            continue
        rel = path.relative_to(ROOT)
        if ".git" in rel.parts:
            continue
        yield path, rel


def main() -> int:
    errors: list[str] = []
    warnings: list[str] = []

    for required in sorted(REQUIRED):
        if not (ROOT / required).exists():
            errors.append(f"missing required file: {required}")

    for path, rel in tracked_candidates():
        lower_parts = {part.lower() for part in rel.parts}
        if path.name in FORBIDDEN_NAMES:
            errors.append(f"forbidden secret/config file: {rel}")
        if path.suffix.lower() in FORBIDDEN_SUFFIXES:
            errors.append(f"forbidden key/keystore file: {rel}")
        if rel.parts and rel.parts[0] == "research" and lower_parts & FORBIDDEN_DIR_PARTS:
            errors.append(f"raw/private research path must not be committed: {rel}")
        try:
            size = path.stat().st_size
        except OSError:
            continue
        if size > MAX_FILE_BYTES:
            warnings.append(f"large file (>5 MiB), review before committing: {rel}")

    for item in warnings:
        print(f"WARNING: {item}")
    for item in errors:
        print(f"ERROR: {item}")

    if errors:
        print(f"\nRepository hygiene failed with {len(errors)} error(s).")
        return 1

    print("Repository hygiene check passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
