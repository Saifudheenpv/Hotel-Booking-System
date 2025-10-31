pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'saifudheenpv/hotel-booking-system'
        DOCKER_TAG = "${env.BUILD_ID}"
        DEV_SERVER = '43.204.234.54'
        SONAR_SERVER = '13.233.38.12'
    }
    
    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }
    
    stages {
        stage('Checkout & Validate') {
            steps {
                checkout scm
                script {
                    echo '🔍 Validating project structure...'
                    if (!fileExists('pom.xml')) {
                        error('❌ pom.xml not found!')
                    }
                    echo '✅ Project structure validated'
                }
            }
        }
        
        stage('Build & Test') {
            steps {
                echo '🏗️ Building and testing...'
                sh 'mvn clean package -Dspring.profiles.active=test -Dserver.port=0'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('Sonar-Server') {
                    echo '🔍 Running SonarQube analysis...'
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.projectKey=hotel-booking-system \
                        -Dsonar.projectName='Hotel Booking System' \
                        -Dsonar.host.url=http://${SONAR_SERVER}:9000 \
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
        
        stage('Quality Gate') {
            steps {
                script {
                    echo '⏳ Waiting for Quality Gate...'
                    // Reduced timeout since webhook will trigger immediately
                    timeout(time: 2, unit: 'MINUTES') {
                        waitForQualityGate abortPipeline: true
                    }
                }
            }
        }
        
        stage('Build Docker Image') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    echo '🐳 Building Docker image...'
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
        
        stage('Push Docker Image') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    echo '📤 Pushing Docker image...'
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh """
                            docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker push ${DOCKER_IMAGE}:latest
                        """
                    }
                }
            }
        }
        
        stage('Deploy to Dev') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                sshagent(['ubuntu-ssh-key']) {
                    script {
                        echo '🚀 Deploying to development server...'
                        sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEV_SERVER} '
                                docker stop hotel-booking-app || true
                                docker rm hotel-booking-app || true
                                docker pull ${DOCKER_IMAGE}:latest
                                docker run -d \\
                                    --name hotel-booking-app \\
                                    --restart unless-stopped \\
                                    --network host \\
                                    -e SPRING_PROFILES_ACTIVE=docker \\
                                    -p 8080:8080 \\
                                    ${DOCKER_IMAGE}:latest
                                sleep 30
                            '
                        """
                    }
                }
            }
        }
        
        stage('Smoke Test') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    echo '🚀 Running smoke tests...'
                    retry(3) {
                        sleep 20
                        sh """
                            curl -f http://${DEV_SERVER}:8080/actuator/health && \\
                            curl -f http://${DEV_SERVER}:8080/ && \\
                            curl -f http://${DEV_SERVER}:8080/hotels
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
            echo "📊 Build Result: ${currentBuild.result}"
        }
        success {
            emailext (
                subject: '✅ SUCCESS: Hotel Booking System CI/CD',
                body: """
                Pipeline completed successfully!
                Build: ${env.BUILD_URL}
                App: http://${DEV_SERVER}:8080
                Sonar: http://${SONAR_SERVER}:9000
                """,
                to: 'mesaifudheenpv@gmail.com'
            )
        }
        failure {
            emailext (
                subject: '❌ FAILED: Hotel Booking System CI/CD',
                body: "Pipeline failed: ${env.BUILD_URL}",
                to: 'mesaifudheenpv@gmail.com'
            )
        }
    }
}