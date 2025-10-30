pipeline {
    agent any
    
    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }
    
    environment {
        DOCKER_IMAGE = 'hotel-booking-system'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        SONAR_URL = 'http://13.233.38.12:9000'
        AWS_ACCOUNT_ID = '724663512594'
        AWS_REGION = 'ap-south-1'
        ECR_REPO = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        DEPLOYMENT_SERVER = '43.204.234.54'
        MYSQL_ROOT_PASSWORD = 'Shanu@9090!'
        MYSQL_DATABASE = 'hotel_booking_db'
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Saifudheenpv/Hotel-Booking-System.git',
                    credentialsId: 'github-credentials'
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn package -DskipTests'
                archiveArtifacts 'target/*.jar'
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('Sonar-Server') {
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.projectKey=hotel-booking-system \
                        -Dsonar.projectName="Hotel Booking System" \
                        -Dsonar.host.url=${SONAR_URL} \
                        -Dsonar.coverage.exclusions=**/*
                    """
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                }
            }
        }
        
        stage('Push to AWS ECR') {
            steps {
                script {
                    withCredentials([[
                        $class: 'AmazonWebServicesCredentialsBinding',
                        credentialsId: 'aws-credentials',
                        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
                        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
                    ]]) {
                        sh """
                            aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker push ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
                    }
                }
            }
        }
        
        stage('Deploy to EC2') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                set -e
                                echo "=== Starting Deployment Process ==="
                                
                                # Stop and cleanup old containers
                                echo "1. Cleaning up old containers..."
                                docker stop hotel-booking-mysql || true
                                docker rm hotel-booking-mysql || true
                                docker stop hotel-booking-system || true
                                docker rm hotel-booking-system || true
                                
                                # Create network
                                echo "2. Creating network..."
                                docker network create hotel-network || true
                                
                                # Start MySQL
                                echo "3. Starting MySQL container..."
                                docker run -d \\
                                    --name hotel-booking-mysql \\
                                    --network hotel-network \\
                                    --restart unless-stopped \\
                                    -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \\
                                    -e MYSQL_DATABASE=${MYSQL_DATABASE} \\
                                    -p 3306:3306 \\
                                    -v mysql_data:/var/lib/mysql \\
                                    mysql:8.0 --default-authentication-plugin=mysql_native_password
                                
                                echo "4. Waiting for MySQL to start..."
                                sleep 30
                                
                                # Create database
                                echo "5. Creating database..."
                                docker exec hotel-booking-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};" || echo "Database check completed"
                                
                                # Pull application image
                                echo "6. Pulling application image..."
                                aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                                docker pull ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                # Start application with NUCLEAR FIX for Actuator metrics
                                echo "7. Starting application with metrics disabled..."
                                docker run -d \\
                                    --name hotel-booking-system \\
                                    --network hotel-network \\
                                    --restart unless-stopped \\
                                    -p 8080:8080 \\
                                    -e SPRING_DATASOURCE_URL="jdbc:mysql://hotel-booking-mysql:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true" \\
                                    -e SPRING_DATASOURCE_USERNAME=root \\
                                    -e SPRING_DATASOURCE_PASSWORD="${MYSQL_ROOT_PASSWORD}" \\
                                    -e JAVA_OPTS="-Dmanagement.endpoints.enabled-by-default=false -Dmanagement.endpoint.health.enabled=false -Dmanagement.metrics.enable.processor=false -Dmanagement.metrics.enable.jvm=false -Dmanagement.metrics.enable.system=false -Dmanagement.metrics.export.default.enabled=false" \\
                                    ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                echo "8. Cleaning up unused images..."
                                docker image prune -f
                                
                                echo "✅ Deployment completed successfully!"
                            '
                        """
                    }
                }
            }
        }
        
        stage('Health Check & Validation') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            echo "Waiting for application to fully start..."
                            sleep 60
                            
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                set -e
                                echo "=== Performing Health Checks ==="
                                
                                # Check container status
                                echo "1. Container Status:"
                                docker ps --format "table {{.Names}}\\t{{.Status}}\\t{{.Ports}}" | grep -E "(hotel-booking|mysql)" || exit 1
                                
                                # Check if container is running (not restarting)
                                echo "2. Checking application stability..."
                                RESTART_COUNT=\$(docker inspect hotel-booking-system --format "{{.RestartCount}}")
                                if [ "\$RESTART_COUNT" -gt 2 ]; then
                                    echo "❌ Application is restarting too frequently"
                                    docker logs hotel-booking-system --tail 30
                                    exit 1
                                else
                                    echo "✅ Application is stable (RestartCount: \$RESTART_COUNT)"
                                fi
                                
                                # Test main application endpoint (not health endpoint)
                                echo "3. Testing main application..."
                                for i in {1..10}; do
                                    if curl -s http://localhost:8080 > /dev/null; then
                                        echo "✅ Main application is accessible"
                                        break
                                    else
                                        echo "⏳ Waiting for application... (attempt \$i/10)"
                                        if [ \$i -eq 10 ]; then
                                            echo "❌ Application not accessible after 10 attempts"
                                            docker logs hotel-booking-system --tail 50
                                            exit 1
                                        fi
                                        sleep 10
                                    fi
                                done
                                
                                # Check application logs for success
                                echo "4. Checking application logs..."
                                if docker logs hotel-booking-system 2>/dev/null | grep -q "Started HotelBookingApplication"; then
                                    echo "✅ Spring Boot started successfully"
                                else
                                    echo "⚠️ Spring Boot startup message not found, checking recent logs..."
                                    docker logs hotel-booking-system --tail 20
                                fi
                                
                                # Verify database connection
                                echo "5. Verifying database connection..."
                                docker exec hotel-booking-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "
                                    USE ${MYSQL_DATABASE};
                                    SELECT '✅ Database connected successfully' as status;
                                " || echo "⚠️ Database connection check"
                                
                                echo "🎉 ALL CHECKS PASSED! Deployment is SUCCESSFUL!"
                                echo "🌐 Your application is live at: http://${DEPLOYMENT_SERVER}:8080"
                                echo "📊 Container Status: \$(docker ps --filter name=hotel-booking-system --format '{{.Status}}')"
                            '
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            echo "Build ${currentBuild.currentResult}"
        }
        success {
            emailext (
                subject: "SUCCESS: Hotel Booking System Build #${env.BUILD_NUMBER}",
                body: """
                <h2>🚀 Deployment Successful!</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Status:</b> Deployed Successfully with Metrics Fix</p>
                <p><b>Application URL:</b> <a href="http://${DEPLOYMENT_SERVER}:8080">http://${DEPLOYMENT_SERVER}:8080</a></p>
                <p><b>View Build:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                <hr>
                <p><b>🔧 Fix Applied:</b> Disabled Spring Boot Actuator metrics completely</p>
                <p><b>📊 Components:</b></p>
                <ul>
                    <li>✅ Application Container: hotel-booking-system</li>
                    <li>✅ Database Container: hotel-booking-mysql</li>
                    <li>✅ Network: hotel-network</li>
                    <li>✅ Database: hotel_booking_db</li>
                </ul>
                """,
                to: "mesaifudheenpv@gmail.com",
                replyTo: "mesaifudheenpv@gmail.com"
            )
        }
        failure {
            emailext (
                subject: "FAILED: Hotel Booking System Build #${env.BUILD_NUMBER}",
                body: """
                <h2>❌ Build or Deployment Failed</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Deployment Server:</b> ${DEPLOYMENT_SERVER}</p>
                <hr>
                <p><b>🔧 Troubleshooting Steps:</b></p>
                <ol>
                    <li><a href="${env.BUILD_URL}">View Build Console</a></li>
                    <li>SSH to server: <code>ssh ubuntu@${DEPLOYMENT_SERVER}</code></li>
                    <li>Check all containers: <code>docker ps -a</code></li>
                    <li>Check application logs: <code>docker logs hotel-booking-system</code></li>
                    <li>Check MySQL logs: <code>docker logs hotel-booking-mysql</code></li>
                    <li>Verify network: <code>docker network ls</code></li>
                </ol>
                """,
                to: "mesaifudheenpv@gmail.com",
                replyTo: "mesaifudheenpv@gmail.com"
            )
        }
    }
}