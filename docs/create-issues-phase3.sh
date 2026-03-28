#!/bin/bash
# Run after: gh auth login
# Creates all Phase 3 GitHub issues

REPO="Onolax/Poke-Battle"

gh label create "phase-3" --repo $REPO --color "#d93f0b" --description "Phase 3: DevOps" 2>/dev/null

gh issue create --repo $REPO \
  --title "Task 8: Multi-stage Dockerfiles for all 6 services" \
  --label "phase-3" \
  --body "## Context
Full plan: \`docs/superpowers/plans/2026-03-28-phase3-devops.md\`

## Goal
Multi-stage Dockerfiles for api-gateway, user-service, game-data-service, lobby-service, battle-service, rating-service.
Build stage: maven:3.9-eclipse-temurin-17. Runtime stage: eclipse-temurin:17-jre-alpine.
Build context is always the project root.

## Files
- Create: \`api-gateway/Dockerfile\`
- Create: \`user-service/Dockerfile\`
- Create: \`game-data-service/Dockerfile\`
- Create: \`lobby-service/Dockerfile\`
- Create: \`battle-service/Dockerfile\`
- Create: \`rating-service/Dockerfile\`

## Steps
- [ ] Create all 6 Dockerfiles (see plan for exact content)
- [ ] Build each image from project root: \`docker build -f api-gateway/Dockerfile -t api-gateway:test .\`
- [ ] Verify all 6 build without errors
- [ ] Commit: \`feat(devops): multi-stage Dockerfiles for all 6 services\`"

gh issue create --repo $REPO \
  --title "Task 9: Full Docker Compose stack (all services + config)" \
  --label "phase-3" \
  --body "## Context
Full plan: \`docs/superpowers/plans/2026-03-28-phase3-devops.md\`
Depends on: Task 8

## Goal
Update application.yml configs to use env vars for all service URLs/connections.
Update infra/docker-compose.yml to add all 6 app services.
Create infra/docker-compose.jenkins.yml for Jenkins + local registry.
Create infra/jenkins/Dockerfile for custom Jenkins image.

## Files
- Modify: \`api-gateway/src/main/resources/application.yml\`
- Modify: \`user-service/src/main/resources/application.yml\`
- Modify: \`game-data-service/src/main/resources/application.yml\`
- Modify: \`lobby-service/src/main/resources/application.yml\`
- Modify: \`battle-service/src/main/resources/application.yml\`
- Modify: \`rating-service/src/main/resources/application.yml\`
- Modify: \`infra/docker-compose.yml\`
- Create: \`infra/docker-compose.jenkins.yml\`
- Create: \`infra/jenkins/Dockerfile\`

## Steps
- [ ] Update all application.yml files (see plan for exact content)
- [ ] Update infra/docker-compose.yml to add 6 services
- [ ] Create infra/docker-compose.jenkins.yml
- [ ] Create infra/jenkins/Dockerfile
- [ ] Run: cd infra && docker compose up -d && docker compose ps
- [ ] Verify all containers healthy
- [ ] Commit: \`feat(devops): full docker compose stack, env-var config\`"

gh issue create --repo $REPO \
  --title "Task 10: Kubernetes manifests on minikube" \
  --label "phase-3" \
  --body "## Context
Full plan: \`docs/superpowers/plans/2026-03-28-phase3-devops.md\`
Depends on: Task 8

## Goal
Kubernetes manifests for all 6 services + infra (Postgres, MongoDB, Redis).
Namespace poke-battle. ConfigMap + Secrets. HPA on battle-service.

## Files
- Create: \`infra/k8s/namespace.yml\`
- Create: \`infra/k8s/configmap.yml\`
- Create: \`infra/k8s/secrets.yml\`
- Create: \`infra/k8s/postgres.yml\`
- Create: \`infra/k8s/mongodb.yml\`
- Create: \`infra/k8s/redis.yml\`
- Create: \`infra/k8s/api-gateway.yml\`
- Create: \`infra/k8s/user-service.yml\`
- Create: \`infra/k8s/game-data-service.yml\`
- Create: \`infra/k8s/lobby-service.yml\`
- Create: \`infra/k8s/battle-service.yml\`
- Create: \`infra/k8s/rating-service.yml\`
- Create: \`infra/k8s/battle-service-hpa.yml\`

## Steps
- [ ] Create all manifest files (see plan for exact content)
- [ ] minikube start --driver=docker
- [ ] Build and load images into minikube
- [ ] kubectl apply -f infra/k8s/
- [ ] kubectl get pods -n poke-battle — all Running
- [ ] Commit: \`feat(devops): kubernetes manifests for all services\`"

gh issue create --repo $REPO \
  --title "Task 11: Jenkins CI/CD pipeline" \
  --label "phase-3" \
  --body "## Context
Full plan: \`docs/superpowers/plans/2026-03-28-phase3-devops.md\`
Depends on: Tasks 8, 9, 10

## Goal
Jenkinsfile at project root. 5 stages: Build, Test, Docker Build, Minikube Load, Deploy.

## Files
- Create: \`Jenkinsfile\`

## Steps
- [ ] Create Jenkinsfile (see plan for exact content)
- [ ] Start Jenkins: docker compose -f infra/docker-compose.jenkins.yml up -d
- [ ] Configure pipeline in Jenkins UI
- [ ] Run build — all 5 stages green
- [ ] Commit: \`feat(devops): Jenkins CI/CD pipeline\`"

echo "All Phase 3 issues created."
