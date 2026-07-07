#!/usr/bin/env bash
set -euo pipefail

image="myapp:dev"
skip_tests=false
up=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --image|-i)
      image="$2"
      shift 2
      ;;
    --skip-tests)
      skip_tests=true
      shift
      ;;
    --up)
      up=true
      shift
      ;;
    --help|-h)
      echo "Usage: scripts/dev-build.sh [--skip-tests] [--up] [--image IMAGE]"
      exit 0
      ;;
    *)
      echo "Unknown option: $1" >&2
      echo "Usage: scripts/dev-build.sh [--skip-tests] [--up] [--image IMAGE]" >&2
      exit 1
      ;;
  esac
done

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
root="$(cd "$script_dir/.." && pwd)"
cd "$root"

maven_args=(clean package jib:buildTar "-Djib.to.image=$image")

if [[ "$skip_tests" == true ]]; then
  maven_args+=("-DskipTests")
fi

echo "Building $image with Jib tar output..."
./mvnw.cmd "${maven_args[@]}"

echo "Loading $image into Docker..."
docker load --input target/jib-image.tar

if [[ "$up" == true ]]; then
  echo "Starting dev Compose stack..."
  docker compose -f compose.yaml -f docker-compose.dev.yaml up -d
fi
