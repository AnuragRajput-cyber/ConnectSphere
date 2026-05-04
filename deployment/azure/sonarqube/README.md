# ConnectSphere SonarQube Setup

This folder runs a self-hosted SonarQube Community dashboard for the Azure VM deployment.

## What This Adds

- SonarQube dashboard on `sonar.anuragbuilds.dev`.
- PostgreSQL database for SonarQube.
- Jenkins pipeline analysis through `sonar-scanner`.
- Jenkins quality gate enforcement before Docker deployment.

## VM Setup

Run these once on the Azure VM.

```bash
sudo sysctl -w vm.max_map_count=262144
echo 'vm.max_map_count=262144' | sudo tee /etc/sysctl.d/99-sonarqube.conf
sudo sysctl --system
```

Create a private env file:

```bash
cd /opt/ConnectSphere/deployment/azure/sonarqube
cp .env.example .env
nano .env
```

Set a strong `SONAR_POSTGRES_PASSWORD`.

Start SonarQube:

```bash
docker compose --env-file .env -f docker-compose.sonarqube.yml up -d
docker logs -f connectsphere-sonarqube
```

Open the dashboard locally from the VM:

```bash
curl -I http://127.0.0.1:9000
```

## Nginx Dashboard Access

Copy the Nginx config:

```bash
sudo cp /opt/ConnectSphere/deployment/azure/nginx/sonar.anuragbuilds.dev.conf /etc/nginx/sites-available/sonar.anuragbuilds.dev
sudo ln -sf /etc/nginx/sites-available/sonar.anuragbuilds.dev /etc/nginx/sites-enabled/sonar.anuragbuilds.dev
sudo nginx -t
sudo systemctl reload nginx
```

Add DNS:

```text
sonar.anuragbuilds.dev -> your Azure VM public IP
```

Create TLS certificate:

```bash
sudo certbot --nginx -d sonar.anuragbuilds.dev
```

Then open:

```text
https://sonar.anuragbuilds.dev
```

Default first login is usually:

```text
username: admin
password: admin
```

Change it immediately.

## SonarQube Project and Token

In SonarQube:

1. Create project key: `connectsphere`.
2. Create a user token for Jenkins.
You do not need to configure the Jenkins SonarQube plugin for this pipeline. The Jenkinsfile calls `sonar-scanner` directly and polls the SonarQube REST API for the quality gate result.

## Jenkins Configuration

Install/rebuild Jenkins with the updated image. The updated Dockerfile installs `sonar-scanner`, which the Jenkinsfile uses directly.

```bash
cd /opt/ConnectSphere/deployment/azure/jenkins
docker compose -f docker-compose.jenkins.yml up -d --build
```

In Jenkins:

1. Go to `Manage Jenkins` -> `Credentials`.
2. Add credential:
   - Kind: Secret text
   - Secret: SonarQube token
   - ID: `sonarqube-token`

Verify Jenkins can reach SonarQube:

```bash
docker exec connectsphere-jenkins curl -I http://connectsphere-sonarqube:9000
docker exec connectsphere-jenkins sonar-scanner --version
```

The Jenkins container is attached to the SonarQube Docker network by `docker-compose.jenkins.yml`, so it can reach SonarQube through the container name `connectsphere-sonarqube`.

## Pipeline Behavior

The production Jenkins pipeline now runs:

1. Backend tests
2. Frontend tests/build
3. SonarQube analysis
4. SonarQube quality gate
5. Docker Compose validation/build/deploy
6. Smoke checks

If the quality gate fails, deployment stops.

## Useful Commands

```bash
docker compose --env-file /opt/ConnectSphere/deployment/azure/sonarqube/.env \
  -f /opt/ConnectSphere/deployment/azure/sonarqube/docker-compose.sonarqube.yml ps

docker logs --tail=120 connectsphere-sonarqube
docker logs --tail=120 connectsphere-sonarqube-db

docker exec connectsphere-jenkins sonar-scanner --version
```
