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
                    if (!fileExists('src/main/java')) {
                        error('❌ Source directory not found!')
                    }
                    echo '✅ Project structure validated'
                }
            }
        }
        
        stage('Build & Compile') {
            steps {
                echo '🏗️ Building Hotel Booking System...'
                sh 'mvn clean compile -DskipTests'
                echo '✅ Build completed successfully!'
            }
        }
        
        stage('Unit Tests') {
            steps {
                echo '🧪 Running unit tests...'
                sh 'mvn test -Dspring.profiles.active=test'
                echo '✅ Tests execution completed'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    echo '✅ Test reports published to Jenkins'
                }
            }
        }
        
        stage('Code Quality Analysis') {
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
                        -Dsonar.tests=src/test/java
                    """
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh """
                            docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                        """
                        sh 'docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD'
                    }
                    echo '✅ Docker image built successfully!'
                }
            }
        }
        
        stage('Push Docker Image') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    echo '📤 Pushing Docker image to registry...'
                    sh """
                        docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                    """
                    echo '✅ Docker image pushed successfully!'
                }
            }
        }
        
        stage('Deploy to Dev Server') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                sshagent(['ubuntu-ssh-key']) {
                    script {
                        echo '🚀 Deploying to development server...'
                        sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEV_SERVER} '
                                # Stop and remove existing containers
                                docker stop hotel-booking-app || true
                                docker rm hotel-booking-app || true
                                docker stop hotel-booking-mysql || true
                                docker rm hotel-booking-mysql || true
                                
                                # Pull latest image
                                docker pull ${DOCKER_IMAGE}:latest
                                
                                # Start MySQL
                                docker run -d \
                                    --name hotel-booking-mysql \
                                    --restart unless-stopped \
                                    -e MYSQL_ROOT_PASSWORD=Shanu@9090! \
                                    -e MYSQL_DATABASE=hotel_booking_db \
                                    -p 3306:3306 \
                                    -v mysql_data:/var/lib/mysql \
                                    mysql:8.0
                                
                                # Wait for MySQL to be ready
                                sleep 30
                                
                                # Start application
                                docker run -d \
                                    --name hotel-booking-app \
                                    --restart unless-stopped \
                                    --link hotel-booking-mysql:mysql \
                                    -e SPRING_PROFILES_ACTIVE=docker \
                                    -e SPRING_DATASOURCE_URL=jdbc:mysql://hotel-booking-mysql:3306/hotel_booking_db?useSSL=false\&serverTimezone=UTC\&allowPublicKeyRetrieval=true \
                                    -e SPRING_DATASOURCE_USERNAME=root \
                                    -e SPRING_DATASOURCE_PASSWORD=Shanu@9090! \
                                    -p 8080:8080 \
                                    ${DOCKER_IMAGE}:latest
                            '
                        """
                        echo '✅ Application deployed successfully!'
                    }
                }
            }
        }
        
        stage('Health Check') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    echo '🏥 Performing health check...'
                    retry(5) {
                        sleep 30
                        sh """
                            curl -f http://${DEV_SERVER}:8080/actuator/health || exit 1
                        """
                    }
                    echo '✅ Health check passed!'
                }
            }
        }
        
        stage('Smoke Tests') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                script {
                    echo '🚀 Running smoke tests...'
                    sh """
                        curl -f http://${DEV_SERVER}:8080/ || exit 1
                        curl -f http://${DEV_SERVER}:8080/hotels || exit 1
                    """
                    echo '✅ Smoke tests passed!'
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline execution completed'
            cleanWs()
        }
        success {
            emailext (
                subject: '✅ SUCCESS: Hotel Booking System CI/CD Pipeline',
                body: """
                The Hotel Booking System CI/CD pipeline completed successfully!
                
                Build: ${env.BUILD_URL}
                Commit: ${env.GIT_COMMIT}
                Application URL: http://${DEV_SERVER}:8080
                SonarQube: http://${SONAR_SERVER}:9000
                """,
                to: 'mesaifudheenpv@gmail.com'
            )
        }
        failure {
            emailext (
                subject: '❌ FAILURE: Hotel Booking System CI/CD Pipeline',
                body: """
                The Hotel Booking System CI/CD pipeline failed!
                
                Build: ${env.BUILD_URL}
                Commit: ${env.GIT_COMMIT}
                
                Please check Jenkins logs for details.
                """,
                to: 'mesaifudheenpv@gmail.com'
            )
            script {
                sshagent(['ubuntu-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${DEV_SERVER} '
                            echo "=== Application Logs ==="
                            docker logs hotel-booking-app --tail 50 || true
                            echo "=== Docker Containers ==="
                            docker ps -a || true
                            echo "=== System Resources ==="
                            docker stats --no-stream || true
                        '
                    """
                }
            }
        }
    }
}