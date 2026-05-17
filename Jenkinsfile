pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'your-registry.com'
        IMAGE_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Backend Tests') {
            steps {
                dir('pos-backend') {
                    sh 'mvn clean test'
                }
            }
        }

        stage('Frontend Tests') {
            steps {
                dir('pos-frontend') {
                    sh 'npm ci && npm run build'
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                sh "docker build -t ${DOCKER_REGISTRY}/fastpos-backend:${IMAGE_TAG} ./pos-backend"
                sh "docker build -t ${DOCKER_REGISTRY}/fastpos-frontend:${IMAGE_TAG} ./pos-frontend"
            }
        }

        stage('Push Images') {
            steps {
                withDockerRegistry([credentialsId: 'docker-credentials', url: "https://${DOCKER_REGISTRY}"]) {
                    sh "docker push ${DOCKER_REGISTRY}/fastpos-backend:${IMAGE_TAG}"
                    sh "docker push ${DOCKER_REGISTRY}/fastpos-frontend:${IMAGE_TAG}"
                }
            }
        }

        stage('Deploy to AWS') {
            steps {
                sh '''
                    ssh -o StrictHostKeyChecking=no ec2-user@your-ec2-host \
                    "cd /opt/fastpos && docker-compose pull && docker-compose up -d"
                '''
            }
        }
    }

    post {
        success { echo 'Pipeline completed successfully!' }
        failure { echo 'Pipeline failed!' }
    }
}
