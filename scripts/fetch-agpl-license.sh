#!/usr/bin/env sh
set -eu
ROOT="$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)"
curl -fL https://www.gnu.org/licenses/agpl-3.0.txt -o "$ROOT/LICENSE"
printf '%s\n' "Downloaded official GNU AGPL v3 text to $ROOT/LICENSE"
