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
                                # Stop and cleanup old containers
                                docker stop hotel-booking-mysql || true
                                docker rm hotel-booking-mysql || true
                                docker stop hotel-booking-system || true
                                docker rm hotel-booking-system || true
                                
                                # Create network
                                docker network create hotel-network || true
                                
                                # Start MySQL
                                docker run -d \\
                                    --name hotel-booking-mysql \\
                                    --network hotel-network \\
                                    --restart unless-stopped \\
                                    -e MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD} \\
                                    -e MYSQL_DATABASE=${MYSQL_DATABASE} \\
                                    -p 3306:3306 \\
                                    -v mysql_data:/var/lib/mysql \\
                                    mysql:8.0 --default-authentication-plugin=mysql_native_password
                                
                                echo "Waiting for MySQL to start..."
                                sleep 30
                                
                                # Create database
                                docker exec hotel-booking-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};" || echo "Database creation check"
                                
                                # Pull and run application
                                aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                                docker pull ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                docker run -d \\
                                    --name hotel-booking-system \\
                                    --network hotel-network \\
                                    --restart unless-stopped \\
                                    -p 8080:8080 \\
                                    -e SPRING_DATASOURCE_URL="jdbc:mysql://hotel-booking-mysql:3306/${MYSQL_DATABASE}?useSSL=false" \\
                                    -e SPRING_DATASOURCE_USERNAME=root \\
                                    -e SPRING_DATASOURCE_PASSWORD="${MYSQL_ROOT_PASSWORD}" \\
                                    -e MANAGEMENT_METRICS_ENABLED=false \\
                                    -e MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info \\
                                    ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                docker image prune -f
                                echo "Deployment completed!"
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
                            sleep 30
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                echo "=== Deployment Status ==="
                                docker ps
                                echo "=== Application Health ==="
                                curl -f http://localhost:8080/actuator/health || curl -s http://localhost:8080/actuator/health || echo "Health endpoint not ready yet"
                                echo "=== Application Logs ==="
                                docker logs hotel-booking-system --tail 20
                                echo "🎉 Application deployed to: http://${DEPLOYMENT_SERVER}:8080"
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
                <h2>Build & Deployment Successful</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Status:</b> Deployed Successfully</p>
                <p><b>Application URL:</b> <a href="http://${DEPLOYMENT_SERVER}:8080">http://${DEPLOYMENT_SERVER}:8080</a></p>
                <p><b>View Build:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                """,
                to: "mesaifudheenpv@gmail.com",
                replyTo: "mesaifudheenpv@gmail.com"
            )
        }
        failure {
            emailext (
                subject: "FAILED: Hotel Booking System Build #${env.BUILD_NUMBER}",
                body: """
                <h2>Build Failed</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>View Build:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                <p><b>Console Output:</b> <a href="${env.BUILD_URL}/console">View Logs</a></p>
                """,
                to: "mesaifudheenpv@gmail.com",
                replyTo: "mesaifudheenpv@gmail.com"
            )
        }
    }
}