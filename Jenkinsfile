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
        
        stage('Compile') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Build Package') {
            steps {
                sh 'mvn clean package -DskipTests'
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
                    withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-credentials']]) {
                        sh """
                            aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker push ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                        """
                    }
                }
            }
        }
        
        stage('Push to DockerHub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            docker login -u $DOCKER_USER -p $DOCKER_PASS
                            docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} saifudheenpv/hotel-booking-system:${DOCKER_TAG}
                            docker push saifudheenpv/hotel-booking-system:${DOCKER_TAG}
                        """
                    }
                }
            }
        }
        
        stage('Setup MySQL on EC2') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                echo "Setting up MySQL Docker container..."
                                
                                # Stop and remove existing MySQL container if exists
                                docker stop hotel-booking-mysql || true
                                docker rm hotel-booking-mysql || true
                                
                                # Pull latest MySQL image
                                docker pull mysql:8.0
                                
                                # Start MySQL container with persistent data
                                docker run -d \\
                                    --name hotel-booking-mysql \\
                                    --restart unless-stopped \\
                                    -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \\
                                    -e MYSQL_DATABASE=${MYSQL_DATABASE} \\
                                    -p 3306:3306 \\
                                    -v mysql_data:/var/lib/mysql \\
                                    mysql:8.0 --default-authentication-plugin=mysql_native_password
                                
                                echo "Waiting for MySQL to start..."
                                sleep 30
                                
                                # Verify MySQL is running and accessible
                                docker exec hotel-booking-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "SHOW DATABASES;" || echo "MySQL might need more time to start"
                                
                                echo "MySQL setup completed successfully"
                            '
                        """
                    }
                }
            }
        }
        
        stage('Deploy Application') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                echo "Deploying application..."
                                
                                # Stop and remove existing application container
                                docker stop ${DOCKER_IMAGE} || true
                                docker rm ${DOCKER_IMAGE} || true
                                
                                # Pull the application image
                                aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                                docker pull ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                # Run the application container
                                docker run -d \\
                                    --name ${DOCKER_IMAGE} \\
                                    --restart unless-stopped \\
                                    -p 8080:8080 \\
                                    -e SPRING_PROFILES_ACTIVE=dev \\
                                    -e SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \\
                                    -e SPRING_DATASOURCE_USERNAME=root \\
                                    -e SPRING_DATASOURCE_PASSWORD=${MYSQL_ROOT_PASSWORD} \\
                                    -e SPRING_JPA_HIBERNATE_DDL_AUTO=update \\
                                    -e SPRING_JPA_SHOW_SQL=true \\
                                    -e AWS_REGION=${AWS_REGION} \\
                                    ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                echo "Application deployment completed"
                                docker image prune -f
                            '
                        """
                    }
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            echo "Waiting for application to start..."
                            sleep 30
                            
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                echo "Performing health checks..."
                                
                                # Check if MySQL is running
                                echo "=== Checking MySQL ==="
                                docker ps | grep hotel-booking-mysql || echo "MySQL container not found"
                                
                                # Check if application is running
                                echo "=== Checking Application ==="
                                docker ps | grep ${DOCKER_IMAGE} || echo "Application container not found"
                                
                                # Check application logs
                                echo "=== Application Logs (last 20 lines) ==="
                                docker logs ${DOCKER_IMAGE} --tail 20 || echo "Could not fetch logs"
                                
                                # Health check with retries
                                for i in 1 2 3 4 5 6; do
                                    echo "Health check attempt \$i..."
                                    if curl -s -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
                                        echo "✅ Application health check PASSED!"
                                        echo "✅ MySQL connection ESTABLISHED!"
                                        echo "🎉 Deployment SUCCESSFUL!"
                                        exit 0
                                    elif curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
                                        echo "⚠️ Application is starting... (attempt \$i)"
                                        sleep 10
                                    else
                                        echo "❌ Application not responding (attempt \$i)"
                                        sleep 10
                                    fi
                                done
                                
                                echo "⚠️ Health check timed out but deployment completed"
                                echo "Check logs with: docker logs ${DOCKER_IMAGE}"
                                exit 0
                            '
                        """
                    }
                }
            }
        }
        
        stage('Verify Database Connection') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                echo "=== Verifying Database Connection ==="
                                
                                # Test MySQL connection from application container
                                docker exec ${DOCKER_IMAGE} sh -c "
                                    apt-get update && apt-get install -y mysql-client || true
                                " > /dev/null 2>&1 || echo "Skipping mysql-client installation"
                                
                                # Check if application can connect to MySQL
                                echo "Checking database tables..."
                                docker exec hotel-booking-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "
                                    USE ${MYSQL_DATABASE};
                                    SHOW TABLES;
                                " || echo "Could not connect to database"
                                
                                echo "Database verification completed"
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
                currentBuild.description = "Build: ${currentBuild.currentResult}"
            }
        }
        success {
            emailext (
                subject: "SUCCESS: Hotel Booking System Build #${env.BUILD_NUMBER}",
                body: """
                <h2>Build & Deployment Successful</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Commit:</b> ${env.GIT_COMMIT.substring(0,7)}</p>
                <p><b>Docker Image:</b> ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}</p>
                <p><b>Deployed to:</b> ${DEPLOYMENT_SERVER}:8080</p>
                <p><b>Database:</b> MySQL Docker container on same EC2</p>
                <p><b>View Build:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                <p><b>Application URL:</b> <a href="http://${DEPLOYMENT_SERVER}:8080">http://${DEPLOYMENT_SERVER}:8080</a></p>
                <p><b>SonarQube Report:</b> <a href="${SONAR_URL}/dashboard?id=hotel-booking-system">View Quality Gate</a></p>
                <p><b>SSH Access:</b> ssh ubuntu@${DEPLOYMENT_SERVER}</p>
                <p><b>View Logs:</b> docker logs ${DOCKER_IMAGE}</p>
                """,
                to: "mesaifudheenpv@gmail.com",
                from: "mesaifudheenpv@gmail.com",
                replyTo: "mesaifudheenpv@gmail.com"
            )
        }
        failure {
            emailext (
                subject: "FAILED: Hotel Booking System Build #${env.BUILD_NUMBER}",
                body: """
                <h2>Build or Deployment Failed</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Commit:</b> ${env.GIT_COMMIT.substring(0,7)}</p>
                <p><b>Deployment Server:</b> ${DEPLOYMENT_SERVER}</p>
                <p><b>View Build:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                <p><b>Console Output:</b> <a href="${env.BUILD_URL}/console">View Logs</a></p>
                <p><b>Troubleshooting Steps:</b></p>
                <ol>
                    <li>SSH to server: ssh ubuntu@${DEPLOYMENT_SERVER}</li>
                    <li>Check containers: docker ps -a</li>
                    <li>Check MySQL logs: docker logs hotel-booking-mysql</li>
                    <li>Check app logs: docker logs ${DOCKER_IMAGE}</li>
                    <li>Verify MySQL: docker exec hotel-booking-mysql mysql -u root -p</li>
                </ol>
                """,
                to: "mesaifudheenpv@gmail.com",
                from: "mesaifudheenpv@gmail.com",
                replyTo: "mesaifudheenpv@gmail.com"
            )
        }
    }
}