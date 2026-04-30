pipeline {
  agent any

  environment {
    COMPOSE_FILE = 'docker-compose.prod.yml'
    DEPLOY_ENV_FILE = 'deployment/.env.production'
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Backend Tests') {
      steps {
        bat 'mvn test'
      }
    }

    stage('Frontend Tests') {
      steps {
        dir('connectsphere-web') {
          bat 'npm ci'
          bat 'npm test -- --watch=false'
          bat 'npm run build'
        }
      }
    }

    stage('Build Docker Images') {
      steps {
        bat 'docker compose --env-file %DEPLOY_ENV_FILE% -f %COMPOSE_FILE% build'
      }
    }

    stage('Deploy Stack') {
      steps {
        bat 'docker compose --env-file %DEPLOY_ENV_FILE% -f %COMPOSE_FILE% up -d'
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'docs/*.md,docs/*.html,docs/*.pdf', allowEmptyArchive: true
    }
  }
}
