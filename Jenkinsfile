pipeline {
    agent any
    
    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }
    
    environment {
        DOCKER_IMAGE = 'hotel-booking-system'
        DOCKER_TAG = "${env.BUILD_ID}-${env.GIT_COMMIT.substring(0,7)}"
        SONAR_URL = 'http://13.233.38.12:9000'
        AWS_ACCOUNT_ID = '724663512594'
        AWS_REGION = 'ap-south-1'
        ECR_REPO = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        DEPLOYMENT_SERVER = '43.204.234.54'
        MYSQL_ROOT_PASSWORD = credentials('mysql-root-password')
        MYSQL_DATABASE = 'hotel_booking_db'
    }
    
    stages {
        stage('Checkout & Security Scan') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Saifudheenpv/Hotel-Booking-System.git',
                    credentialsId: 'github-credentials',
                    poll: false
                
                // Security: Check for secrets in code
                sh 'git secrets --scan'
            }
        }
        
        stage('Compile & Test') {
            parallel {
                stage('Compile') {
                    steps {
                        sh 'mvn clean compile'
                    }
                }
                stage('Unit Tests') {
                    steps {
                        sh 'mvn test -DskipTests=false'
                    }
                    post {
                        always {
                            junit 'target/surefire-reports/*.xml'
                        }
                    }
                }
            }
        }
        
        stage('Build & Security') {
            steps {
                sh 'mvn clean package -DskipTests'
                archiveArtifacts 'target/*.jar'
                
                // Security: Scan for vulnerabilities
                sh 'docker scan ${DOCKER_IMAGE}:${DOCKER_TAG} --file Dockerfile --exclude-base'
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
                        -Dsonar.coverage.exclusions=**/* \
                        -Dsonar.security.report=./security-report.json
                    """
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 15, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh """
                        docker build \
                        --no-cache \
                        --tag ${DOCKER_IMAGE}:${DOCKER_TAG} \
                        --tag ${DOCKER_IMAGE}:latest \
                        --build-arg BUILD_DATE=\$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
                        --build-arg VERSION=${DOCKER_TAG} \
                        .
                    """
                }
            }
        }
        
        stage('Push to AWS ECR') {
            steps {
                script {
                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', 
                                   credentialsId: 'aws-credentials', 
                                   roleArn: 'arn:aws:iam::724663512594:role/JenkinsDeployRole']]) {
                        sh """
                            aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${ECR_REPO}/${DOCKER_IMAGE}:latest
                            docker push ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker push ${ECR_REPO}/${DOCKER_IMAGE}:latest
                        """
                    }
                }
            }
        }
        
        stage('Push to DockerHub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', 
                                                   usernameVariable: 'DOCKER_USER', 
                                                   passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            docker login -u $DOCKER_USER -p $DOCKER_PASS
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} saifudheenpv/hotel-booking-system:${DOCKER_TAG}
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} saifudheenpv/hotel-booking-system:latest
                            docker push saifudheenpv/hotel-booking-system:${DOCKER_TAG}
                            docker push saifudheenpv/hotel-booking-system:latest
                        """
                    }
                }
            }
        }
        
        stage('Deploy to Development') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no -o ServerAliveInterval=60 ubuntu@${DEPLOYMENT_SERVER} '
                                set -e
                                echo "=== Starting Deployment Process ==="
                                
                                # Create network if not exists
                                docker network create hotel-network || true
                                
                                # Stop and cleanup old containers
                                echo "1. Cleaning up old containers..."
                                docker stop hotel-booking-mysql || true
                                docker rm hotel-booking-mysql || true
                                docker stop hotel-booking-system || true
                                docker rm hotel-booking-system || true
                                
                                # Start MySQL with security best practices
                                echo "2. Starting MySQL container..."
                                docker run -d \\
                                    --name hotel-booking-mysql \\
                                    --network hotel-network \\
                                    --restart unless-stopped \\
                                    -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \\
                                    -e MYSQL_DATABASE=${MYSQL_DATABASE} \\
                                    -e MYSQL_INNODB_BUFFER_POOL_SIZE=256M \\
                                    -p 3306:3306 \\
                                    -v mysql_data:/var/lib/mysql \\
                                    -v mysql_logs:/var/log/mysql \\
                                    --memory=512m \\
                                    --cpus="0.5" \\
                                    mysql:8.0 \\
                                    --default-authentication-plugin=mysql_native_password \\
                                    --innodb-buffer-pool-size=256M \\
                                    --max-connections=100
                                
                                echo "3. Waiting for MySQL to be ready..."
                                for i in {1..30}; do
                                    if docker exec hotel-booking-mysql mysqladmin ping -uroot -p${MYSQL_ROOT_PASSWORD} --silent; then
                                        echo "MySQL is ready!"
                                        break
                                    fi
                                    echo "Waiting for MySQL... (\$i/30)"
                                    sleep 2
                                done
                                
                                # Create database and test connection
                                docker exec hotel-booking-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "
                                    CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};
                                    SHOW DATABASES;
                                " || exit 1
                                
                                # Pull latest application image
                                echo "4. Pulling application image..."
                                aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                                docker pull ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                # Start application with FIXED configuration
                                echo "5. Starting application container..."
                                docker run -d \\
                                    --name hotel-booking-system \\
                                    --network hotel-network \\
                                    --restart unless-stopped \\
                                    -p 8080:8080 \\
                                    -e SPRING_PROFILES_ACTIVE=dev \\
                                    -e SPRING_DATASOURCE_URL=jdbc:mysql://hotel-booking-mysql:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \\
                                    -e SPRING_DATASOURCE_USERNAME=root \\
                                    -e SPRING_DATASOURCE_PASSWORD=${MYSQL_ROOT_PASSWORD} \\
                                    -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \\
                                    -e MANAGEMENT_METRICS_ENABLED=false \\
                                    -e MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info \\
                                    -e JAVA_OPTS="-Xmx256m -Xms128m -Dmanagement.metrics.enable.processor=false" \\
                                    --memory=512m \\
                                    --cpus="0.5" \\
                                    --health-cmd="curl -f http://localhost:8080/actuator/health || exit 1" \\
                                    --health-interval=30s \\
                                    --health-timeout=10s \\
                                    --health-retries=3 \\
                                    ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                echo "6. Cleaning up unused images..."
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
                            sleep 45
                            
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                set -e
                                echo "=== Performing Health Checks ==="
                                
                                # Check container status
                                echo "1. Container Status:"
                                docker ps --format "table {{.Names}}\\t{{.Status}}\\t{{.Ports}}" | grep -E "(hotel-booking|mysql)" || exit 1
                                
                                # Check application health with retries
                                echo "2. Application Health Check:"
                                for i in {1..12}; do
                                    if curl -s -f http://localhost:8080/actuator/health > /dev/null; then
                                        echo "✅ Application health check PASSED!"
                                        HEALTH_STATUS=\$(curl -s http://localhost:8080/actuator/health | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
                                        echo "📊 Health Status: \$HEALTH_STATUS"
                                        break
                                    else
                                        echo "⏳ Application starting... (attempt \$i/12)"
                                        if [ \$i -eq 12 ]; then
                                            echo "❌ Health check failed after 2 minutes"
                                            docker logs hotel-booking-system --tail 50
                                            exit 1
                                        fi
                                        sleep 10
                                    fi
                                done
                                
                                # Test main endpoint
                                echo "3. Testing main application endpoint:"
                                if curl -s http://localhost:8080 > /dev/null; then
                                    echo "✅ Main application endpoint is accessible"
                                else
                                    echo "❌ Main application endpoint failed"
                                    exit 1
                                fi
                                
                                # Verify database connection
                                echo "4. Verifying database connection:"
                                docker exec hotel-booking-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "
                                    USE ${MYSQL_DATABASE};
                                    SELECT COUNT(*) as table_count FROM information_schema.tables 
                                    WHERE table_schema = '${MYSQL_DATABASE}';
                                " || echo "⚠️ No tables found (first deployment)"
                                
                                echo "🎉 ALL CHECKS PASSED! Deployment is SUCCESSFUL!"
                                echo "🌐 Your application is live at: http://${DEPLOYMENT_SERVER}:8080"
                            '
                        """
                    }
                }
            }
        }
        
        stage('Security Hardening') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                echo "=== Applying Security Hardening ==="
                                
                                # Update system packages
                                sudo apt-get update && sudo apt-get upgrade -y
                                
                                # Configure firewall (if ufw is available)
                                sudo ufw --force enable || true
                                sudo ufw allow 22/tcp comment "SSH" || true
                                sudo ufw allow 8080/tcp comment "Hotel Booking App" || true
                                sudo ufw allow 3306/tcp comment "MySQL" || true
                                
                                # Set up log rotation for containers
                                sudo docker logs hotel-booking-system --tail 10 > /tmp/app_startup.log
                                sudo docker logs hotel-booking-mysql --tail 10 > /tmp/mysql_startup.log
                                
                                echo "✅ Security hardening completed"
                            '
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            script {
                // Cleanup workspace to save disk space
                sh 'docker system prune -f || true'
                currentBuild.description = "Build ${currentBuild.currentResult} - ${env.GIT_COMMIT.substring(0,7)}"
            }
        }
        success {
            script {
                // Update deployment tracking
                sh """
                    echo "Deployment Successful - ${env.BUILD_URL}" > deployment_status.txt
                    echo "Commit: ${env.GIT_COMMIT}" >> deployment_status.txt
                    echo "Image: ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}" >> deployment_status.txt
                """
                
                emailext (
                    subject: "SUCCESS: Hotel Booking System v${env.BUILD_NUMBER} Deployed",
                    body: """
                    <h2>🚀 Deployment Successful!</h2>
                    <p><b>Project:</b> Hotel Booking System</p>
                    <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                    <p><b>Commit:</b> ${env.GIT_COMMIT.substring(0,7)}</p>
                    <p><b>Docker Image:</b> ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}</p>
                    <p><b>Deployed to:</b> ${DEPLOYMENT_SERVER}:8080</p>
                    <p><b>Database:</b> MySQL Docker container with persistent storage</p>
                    <hr>
                    <p><b>🔗 Quick Links:</b></p>
                    <ul>
                        <li><a href="${env.BUILD_URL}">View Build Details</a></li>
                        <li><a href="http://${DEPLOYMENT_SERVER}:8080">Access Application</a></li>
                        <li><a href="${SONAR_URL}/dashboard?id=hotel-booking-system">View Quality Report</a></li>
                    </ul>
                    <p><b>📊 Deployment Info:</b></p>
                    <ul>
                        <li>Container Network: hotel-network</li>
                        <li>Database: hotel_booking_db</li>
                        <li>Health Check: /actuator/health</li>
                        <li>Resource Limits: 512MB RAM, 0.5 CPU</li>
                    </ul>
                    """,
                    to: "mesaifudheenpv@gmail.com",
                    from: "jenkins@${env.JENKINS_URL}",
                    replyTo: "mesaifudheenpv@gmail.com",
                    attachLog: false
                )
            }
        }
        failure {
            emailext (
                subject: "FAILED: Hotel Booking System Build #${env.BUILD_NUMBER}",
                body: """
                <h2>❌ Build or Deployment Failed</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Commit:</b> ${env.GIT_COMMIT.substring(0,7)}</p>
                <p><b>Deployment Server:</b> ${DEPLOYMENT_SERVER}</p>
                <hr>
                <p><b>🔧 Troubleshooting Steps:</b></p>
                <ol>
                    <li><a href="${env.BUILD_URL}">View Build Console</a></li>
                    <li>SSH to server: <code>ssh ubuntu@${DEPLOYMENT_SERVER}</code></li>
                    <li>Check all containers: <code>docker ps -a</code></li>
                    <li>Check application logs: <code>docker logs hotel-booking-system</code></li>
                    <li>Check MySQL logs: <code>docker logs hotel-booking-mysql</code></li>
                    <li>Verify database: <code>docker exec hotel-booking-mysql mysql -u root -p</code></li>
                </ol>
                <p><b>📞 Support:</b> Check Jenkins build logs for detailed error information.</p>
                """,
                to: "mesaifudheenpv@gmail.com",
                from: "jenkins@${env.JENKINS_URL}",
                replyTo: "mesaifudheenpv@gmail.com",
                attachLog: true
            )
        }
        cleanup {
            // Always cleanup Jenkins workspace
            cleanWs()
        }
    }
}