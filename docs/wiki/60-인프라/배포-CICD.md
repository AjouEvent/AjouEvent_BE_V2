---
type: 인프라
status: 확정
source: infra.json cicd
updated: 2026-11-15
tags: []
---

# 배포와 CI/CD

| 저장소 | 워크플로 | 트리거 | 단계 | 배포 대상 | 근거 |
|---|---|---|---|---|---|
| AjouEvent_BE_V2 | deploy-oci.yml (Deploy to OCI) | push main, workflow_dispatch; concurrency deploy-oci | checkout@v7; setup-qemu@v4 · setup-buildx@v4; GHCR 로그인(GITHUB_TOKEN); build-push@v7 linux/arm64,linux/amd64, gha 캐시, 태그 latest + sha; scp deploy/{docker-compose.yml,caddy,prometheus,loki,scripts} → ~/ajouevent (strip 1); scp monitoring/grafana → ~/ajouevent/grafana; ssh: docker login ghcr(임시) → pull → compose up -d --remove-orphans → caddy reload → Prometheus /-/reload → 헬스 체크 최대 3분 → image prune | A1 ajouevent-core (secrets.OCI_HOST, user ubuntu, secrets.OCI_DEPLOY_KEY) | .github/workflows/deploy-oci.yml:1-121 |
| AjouEvent_BE_V2 | notion-sync.yml | issues/pull_request opened·reopened·closed | Node 20, scripts/notion-sync → Notion DB | Notion | .github/workflows/notion-sync.yml |
| AjouEvent_Crawling_V2 | ci.yml (CI) | pull_request, push main | setup-go@v7 Go 1.27; go test ./...; docker build --target prod | 없음 (검증) | .github/workflows/ci.yml |
| AjouEvent_Crawling_V2 | deploy-oci.yml (Deploy to OCI) | push main, workflow_dispatch; concurrency deploy-oci | build-push@v7 docker/Dockerfile target prod, linux/amd64,linux/arm64, 태그 latest + sha; scp docker-compose.yml → /home/ubuntu/crawling; ssh: .env 존재 확인 → loki 드라이버 플러그인 3.7.2-<arch> 설치/활성 → compose pull/up → prune | A1 (OCI_HOST/OCI_DEPLOY_KEY) | AjouEvent_Crawling_V2/.github/workflows/deploy-oci.yml:1-95 |
| AjouEvent_FE | deploy-pages.yml (Deploy to Cloudflare Pages) | push main(운영), pull_request(미리보기), workflow_dispatch | setup-node@v7 Node 22, npm ci; npm run build (REACT_APP_* = vars, sourcemap 끔); cloudflare/wrangler-action@v4: pages project create(있으면 무시) → pages deploy build --project-name=ajouevent --branch=<ref> | Cloudflare Pages ajouevent (www.ajouevent.com) | AjouEvent_FE/.github/workflows/deploy-pages.yml:1-62 |
| (공통) | 서버 설정 복구 | 수동 | render-env.sh <secret> <file> 로 Vault → .env 생성 (크롤러 배포는 .env 없으면 실패) | A1 | deploy/scripts/render-env.sh, Crawling deploy-oci.yml:82-85 |
| (폐기) | Deploy to EC2 (deploy.yml, 아올다 포트 20000) · FE Vercel 복사 워크플로 | - | 비활성화 후 삭제, EC2_* · DOCKER_* · FIREBASE_SERVICE_ACCOUNT · FE AWS 키 시크릿 삭제 | - | report §옛 배포 차단 · §정리 결과 |

이전 경과는 [[AWS-OCI-이전]]에 있다. 근거: `infra.json` cicd.
