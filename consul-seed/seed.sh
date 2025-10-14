#!/bin/sh
set -e

echo "[seed] Seeding Consul KV from /seed"
echo "[seed] files in /seed:"
ls -la /seed

# если нет *.yml — выходим тихо
if [ -z "$(ls -1 /seed/*.yml 2>/dev/null)" ]; then
  echo "[seed] no *.yml files found, nothing to do"
  exit 0
fi

for f in /seed/*.yml; do
  name="$(basename "$f" .yml)"
  key="config/${name}/data"
  echo "  -> PUT ${key}"
  curl -fsS -X PUT --data-binary @"$f" "http://consul:8500/v1/kv/${key}" >/dev/null
done

echo "[seed] Done."
