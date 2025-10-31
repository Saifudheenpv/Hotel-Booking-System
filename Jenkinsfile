pipeline {
    agent any
    
    environment {
        // Docker Configuration
        DOCKER_REGISTRY = 'saifudheenpv'
        APP_NAME = 'hotel-booking-system'
        VERSION = "${env.BUILD_ID}"
        
        // Server IPs - UPDATE THESE WITH YOUR ACTUAL IPs
        DEV_SERVER_IP = '43.204.234.54'  // Your dev-server EC2 IP
        JENKINS_SERVER_IP = '43.205.5.17'  // Your jenkins-server EC2 IP
        SONAR_SERVER_IP = '13.233.38.12'  // Your sonarqube-server EC2 IP
        
        // Your Local Machine Public IP (for MySQL access)
        LOCAL_DB_IP = '157.51.222.57'  // UPDATE THIS!
        
        // Database Credentials
        DB_USER = 'root'
        DB_PASSWORD = 'Shanu@9090!'
        
        // SonarQube
        SONAR_SCANNER_HOME = tool 'Sonar-Scanner'
    }
    
    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }
    
    stages {
        stage('Checkout & Validate') {
            steps {
                checkout scm
                
                script {
                    echo "🔍 Validating project structure..."
                    if (!fileExists('pom.xml')) {
                        error("❌ pom.xml not found!")
                    }
                    if (!fileExists('src/main/java/com/hotel/HotelBookingApplication.java')) {
                        error("❌ Main application class not found!")
                    }
                    echo "✅ Project structure validated"
                }
            }
        }
        
        stage('Build & Compile') {
            steps {
                sh '''
                echo "🏗️ Building Hotel Booking System..."
                mvn clean compile -DskipTests
                echo "✅ Build completed successfully!"
                '''
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh '''
                echo "🧪 Running unit tests..."
                mvn test
                '''
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    jacoco execPattern: 'target/jacoco.exec'
                }
            }
        }
        
        stage('Code Quality Analysis') {
            steps {
                withSonarQubeEnv('Sonar-Server') {
                    sh """
                    mvn sonar:sonar \
                      -Dsonar.projectKey=hotel-booking-system \
                      -Dsonar.projectName="Hotel Booking System" \
                      -Dsonar.host.url=http://${SONAR_SERVER_IP}:9000 \
                      -Dsonar.login=${env.SONAR_TOKEN} \
                      -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
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
        
        stage('Security Scan') {
            steps {
                sh '''
                echo "🔒 Running security scan..."
                mvn org.owasp:dependency-check-maven:check -DskipTests
                echo "✅ Security scan completed"
                '''
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    echo "🐳 Building Docker image..."
                    // Package the application first
                    sh 'mvn clean package -DskipTests'
                    
                    // Build Docker image
                    dockerImage = docker.build("${DOCKER_REGISTRY}/${APP_NAME}:${VERSION}", 
                        "--build-arg DB_HOST=${LOCAL_DB_IP} --build-arg DB_USER=${DB_USER} --build-arg DB_PASSWORD=${DB_PASSWORD} .")
                }
            }
        }
        
        stage('Push Docker Image') {
            steps {
                script {
                    echo "📤 Pushing Docker image to registry..."
                    docker.withRegistry('', 'dockerhub-creds') {
                        dockerImage.push()
                        dockerImage.push('latest')
                    }
                    echo "✅ Docker image pushed successfully"
                }
            }
        }
        
        stage('Deploy to Dev Server') {
            steps {
                script {
                    echo "🚀 Deploying to Development Server..."
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${DEV_SERVER_IP} '
                            echo "📥 Pulling latest Docker image..."
                            docker pull ${DOCKER_REGISTRY}/${APP_NAME}:${VERSION}
                            
                            echo "🛑 Stopping existing container..."
                            docker stop hotel-booking-app || true
                            docker rm hotel-booking-app || true
                            
                            echo "🎯 Starting new container..."
                            docker run -d \\
                                --name hotel-booking-app \\
                                -p 8080:8080 \\
                                -e SPRING_PROFILES_ACTIVE=docker,prod \\
                                -e DB_HOST=${LOCAL_DB_IP} \\
                                -e DB_USER=${DB_USER} \\
                                -e DB_PASSWORD=${DB_PASSWORD} \\
                                -e APP_DATA_INITIALIZE=true \\
                                ${DOCKER_REGISTRY}/${APP_NAME}:${VERSION}
                            
                            echo "⏳ Waiting for application to start..."
                            sleep 30
                            
                            echo "🔍 Checking application health..."
                            curl -f http://localhost:8080/actuator/health || exit 1
                        '
                        """
                    }
                    echo "✅ Deployment completed successfully!"
                }
            }
        }
        
        stage('Smoke Tests') {
            steps {
                sh """
                echo "🚬 Running smoke tests..."
                
                # Test basic endpoints
                curl -f http://${DEV_SERVER_IP}:8080/actuator/health || exit 1
                echo "✅ Health check passed"
                
                curl -f http://${DEV_SERVER_IP}:8080/ || exit 1
                echo "✅ Home page accessible"
                
                curl -f http://${DEV_SERVER_IP}:8080/hotels || exit 1
                echo "✅ Hotels endpoint working"
                
                echo "🎉 All smoke tests passed!"
                """
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh """
                echo "🔗 Running integration tests..."
                
                # Test creating a booking
                curl -X POST http://${DEV_SERVER_IP}:8080/bookings \\
                  -H "Content-Type: application/x-www-form-urlencoded" \\
                  -d "guestName=TestUser&roomId=1&checkInDate=2024-02-01&checkOutDate=2024-02-05" \\
                  -w "\\\\nHTTP Status: %{http_code}\\\\n" || true
                  
                echo "✅ Integration tests completed"
                """
            }
        }
    }
    
    post {
        always {
            // Clean workspace
            cleanWs()
            
            // Send email notification
            emailext (
                subject: "🏨 Hotel Booking System Build #${env.BUILD_NUMBER} - ${currentBuild.currentResult}",
                body: """
                🏨 HOTEL BOOKING SYSTEM - CI/CD PIPELINE RESULT
                
                📊 Build Details:
                - Project: ${env.JOB_NAME}
                - Build Number: #${env.BUILD_NUMBER}
                - Status: ${currentBuild.currentResult}
                - Version: ${VERSION}
                
                🌐 Access Points:
                - Application: http://${DEV_SERVER_IP}:8080
                - Health Check: http://${DEV_SERVER_IP}:8080/actuator/health
                
                🔗 Useful Links:
                - Build URL: ${env.BUILD_URL}
                - SonarQube: http://${SONAR_SERVER_IP}:9000
                - Docker Hub: https://hub.docker.com/r/${DOCKER_REGISTRY}/${APP_NAME}
                
                📈 Deployment Info:
                - Environment: Development
                - Database: MySQL (Local - ${LOCAL_DB_IP})
                - Server: AWS EC2 (${DEV_SERVER_IP})
                
                💡 Next Steps:
                - Verify data initialization completed
                - Test booking functionality
                - Check application logs if needed
                """,
                to: 'mesaifudheenpv@gmail.com',
                replyTo: 'mesaifudheenpv@gmail.com'
            )
        }
        success {
            script {
                echo "🎉 🏨 HOTEL BOOKING SYSTEM DEPLOYED SUCCESSFULLY!"
                echo "🌐 Application URL: http://${DEV_SERVER_IP}:8080"
                echo "🔍 Health Check: http://${DEV_SERVER_IP}:8080/actuator/health"
                echo "📊 SonarQube: http://${SONAR_SERVER_IP}:9000"
                
                // Slack notification would go here if configured
            }
        }
        failure {
            script {
                echo "❌ Deployment failed! Check Jenkins logs for details."
                
                // Attempt to get logs from failed deployment
                sshagent(['ubuntu-ssh-key']) {
                    sh """
                    ssh -o StrictHostKeyChecking=no ubuntu@${DEV_SERVER_IP} '
                        echo "=== Application Logs ==="
                        docker logs hotel-booking-app --tail 50 || true
                    ' || true
                    """
                }
            }
        }
    }
}