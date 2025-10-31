pipeline {
    agent any
    
    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }
    
    environment {
        DOCKERHUB_CREDENTIALS = credentials('dockerhub-creds')
        SONAR_TOKEN = credentials('sonar-token')
        DOCKER_IMAGE = 'saifudheenpv/hotel-booking-system:latest'
        DOCKER_REGISTRY = 'docker.io'
    }
    
    stages {
        stage('Checkout & Validate') {
            steps {
                checkout scm
                script {
                    echo "🔍 Validating project structure..."
                    if (!fileExists('pom.xml')) {
                        error "pom.xml not found!"
                    }
                    if (!fileExists('src/main/java')) {
                        error "Source code directory not found!"
                    }
                    echo "✅ Project structure validated"
                }
            }
        }
        
        stage('Build & Test') {
            steps {
                script {
                    echo "🏗️ Building and testing..."
                    sh 'mvn clean test jacoco:report package -Dspring.profiles.active=test -Dserver.port=0'
                }
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/site/jacoco',
                        reportFiles: 'index.html',
                        reportName: 'JaCoCo Code Coverage Report'
                    ])
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    echo "🔍 Running SonarQube analysis..."
                    withSonarQubeEnv('Sonar-Server') {
                        sh """
                            mvn sonar:sonar \
                            -Dsonar.projectKey=hotel-booking-system \
                            -Dsonar.projectName=Hotel Booking System \
                            -Dsonar.host.url=http://13.233.38.12:9000 \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            -Dsonar.junit.reportsPath=target/surefire-reports \
                            -Dsonar.coverage.exclusions=**/config/DataInitializer.java \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.scm.provider=git \
                            -Dsonar.sourceEncoding=UTF-8
                        """
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                script {
                    echo "⏳ Waiting for Quality Gate..."
                    timeout(time: 2, unit: 'MINUTES') {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                    echo "✅ Quality Gate passed!"
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    echo "🐳 Building Docker image..."
                    sh """
                        docker build -t ${DOCKER_IMAGE} .
                    """
                }
            }
        }
        
        stage('Push Docker Image') {
            steps {
                script {
                    echo "📦 Pushing Docker image to registry..."
                    sh """
                        echo \"${DOCKERHUB_CREDENTIALS_PSW}\" | docker login -u \"${DOCKERHUB_CREDENTIALS_USR}\" --password-stdin
                        docker push ${DOCKER_IMAGE}
                    """
                }
            }
        }
        
        stage('Deploy to Dev') {
            steps {
                script {
                    echo "🚀 Deploying to Development Server..."
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@43.204.234.54 '
                                # Stop and remove existing container
                                docker stop hotel-booking-app || true
                                docker rm hotel-booking-app || true
                                
                                # Pull latest image
                                docker pull ${DOCKER_IMAGE}
                                
                                # Run new container
                                docker run -d \
                                    --name hotel-booking-app \
                                    -p 8080:8080 \
                                    -e SPRING_PROFILES_ACTIVE=docker \
                                    --restart unless-stopped \
                                    ${DOCKER_IMAGE}
                                
                                # Clean up old images
                                docker image prune -f
                            '
                        """
                    }
                }
            }
        }
        
        stage('Smoke Test') {
            steps {
                script {
                    echo "🧪 Running smoke tests..."
                    retry(3) {
                        sh """
                            curl -f http://43.204.234.54:8080/actuator/health || exit 1
                            echo "✅ Application is healthy"
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "📊 Build Result: ${currentBuild.result}"
            cleanWs()
        }
        success {
            emailext (
                subject: "✅ BUILD SUCCESS: Hotel Booking System - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                <h2>Build Success!</h2>
                <p><strong>Project:</strong> ${env.JOB_NAME}</p>
                <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                <p><strong>Status:</strong> SUCCESS</p>
                <p><strong>Deployed to:</strong> Development Server</p>
                <p><strong>Application URL:</strong> http://43.204.234.54:8080</p>
                <p><strong>SonarQube Report:</strong> http://13.233.38.12:9000/dashboard?id=hotel-booking-system</p>
                <br/>
                <p>Best regards,<br/>Jenkins CI/CD System</p>
                """,
                to: "mesaifudheenpv@gmail.com"
            )
        }
        failure {
            emailext (
                subject: "❌ BUILD FAILED: Hotel Booking System - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: """
                <h2>Build Failed!</h2>
                <p><strong>Project:</strong> ${env.JOB_NAME}</p>
                <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                <p><strong>Status:</strong> FAILED</p>
                <p><strong>Please check Jenkins for details:</strong> ${env.BUILD_URL}</p>
                <br/>
                <p>Best regards,<br/>Jenkins CI/CD System</p>
                """,
                to: "mesaifudheenpv@gmail.com"
            )
        }
    }
}
