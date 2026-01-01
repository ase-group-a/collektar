set -e

cd "$(dirname "$0")"

BRANCH="$(basename "$(pwd)")"

git fetch
git checkout "$BRANCH"
git reset --hard "origin/$BRANCH"

docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d --build