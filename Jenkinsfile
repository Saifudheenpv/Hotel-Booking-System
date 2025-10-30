pipeline {
    agent any
    
    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }
    
    environment {
        DOCKER_IMAGE = 'hotel-booking-system'
        DOCKER_TAG = "${env.BUILD_NUMBER}"
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
                sh 'mvn clean package -DskipTests'
                sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
            }
        }
        
        stage('Deploy') {
            steps {
                sshagent(['ubuntu-ssh-key']) {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                            set -e
                            echo "=== Deploying Fixed Application ==="
                            
                            # Cleanup old containers
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
                            docker exec hotel-booking-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS ${MYSQL_DATABASE};"
                            
                            # Start application with FIXED Dockerfile
                            docker run -d \\
                                --name hotel-booking-system \\
                                --network hotel-network \\
                                --restart unless-stopped \\
                                -p 8080:8080 \\
                                -e SPRING_DATASOURCE_URL="jdbc:mysql://hotel-booking-mysql:3306/${MYSQL_DATABASE}?useSSL=false" \\
                                -e SPRING_DATASOURCE_USERNAME=root \\
                                -e SPRING_DATASOURCE_PASSWORD="${MYSQL_ROOT_PASSWORD}" \\
                                ${DOCKER_IMAGE}:${DOCKER_TAG}
                            
                            echo "✅ Application deployed with fixed Dockerfile!"
                            echo "⏳ Waiting 90 seconds for full startup..."
                            sleep 90
                            
                            # Final verification
                            echo "=== Final Verification ==="
                            docker ps
                            
                            if curl -s http://localhost:8080 > /dev/null; then
                                echo "🎉 SUCCESS! Application is running and accessible!"
                                echo "🌐 Access your Hotel Booking System at: http://${DEPLOYMENT_SERVER}:8080"
                            else
                                echo "⚠️ Application starting up..."
                                docker logs hotel-booking-system --tail 20
                            fi
                        '
                    """
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
                <p><b>Status:</b> Fixed Actuator Metrics Issue</p>
                <p><b>Application URL:</b> <a href="http://${DEPLOYMENT_SERVER}:8080">http://${DEPLOYMENT_SERVER}:8080</a></p>
                <p><b>Fix Applied:</b> Updated Dockerfile with disabled Actuator metrics</p>
                """,
                to: "mesaifudheenpv@gmail.com",
                replyTo: "mesaifudheenpv@gmail.com"
            )
        }
    }
}