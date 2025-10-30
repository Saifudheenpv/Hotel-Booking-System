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
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/Saifudheenpv/Hotel-Booking-System.git',
                    credentialsId: 'github-credentials'
                
                script {
                    currentBuild.displayName = "#${BUILD_ID}-${env.GIT_COMMIT.substring(0,7)}"
                }
            }
        }
        
        stage('Compile & Unit Tests') {
            steps {
                sh 'mvn clean compile'
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('Sonar-Server') {
                    sh """
                        mvn sonar:sonar \
                        -Dsonar.projectKey=hotel-booking-system \
                        -Dsonar.projectName="Hotel Booking System" \
                        -Dsonar.host.url=${SONAR_URL}
                    """
                }
            }
        }
        
        stage('Build Package') {
            steps {
                sh 'mvn clean package -DskipTests'
                archiveArtifacts 'target/*.jar'
            }
        }
        
        stage('OWASP Dependency Check') {
            steps {
                sh 'mvn org.owasp:dependency-check-maven:check -DskipTests'
            }
            post {
                always {
                    dependencyCheckPublisher pattern: '**/dependency-check-report.xml'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
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
        
        stage('Deploy to Development') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                # Login to AWS ECR
                                aws ecr get-login-password --region ${AWS_REGION} | sudo docker login --username AWS --password-stdin ${ECR_REPO}
                                
                                # Pull latest image
                                sudo docker pull ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                
                                # Stop and remove old container
                                sudo docker stop ${DOCKER_IMAGE} || true
                                sudo docker rm ${DOCKER_IMAGE} || true
                                
                                # Run new container
                                sudo docker run -d \
                                    --name ${DOCKER_IMAGE} \
                                    --restart unless-stopped \
                                    -p 8080:8080 \
                                    -e SPRING_PROFILES_ACTIVE=dev \
                                    -e AWS_REGION=${AWS_REGION} \
                                    ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}
                                    
                                # Clean up old images
                                sudo docker image prune -f
                            '
                        """
                    }
                }
            }
        }
        
        stage('Integration Tests') {
            steps {
                sh 'mvn verify -DskipUnitTests'
            }
            post {
                always {
                    junit 'target/failsafe-reports/*.xml'
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    sshagent(['ubuntu-ssh-key']) {
                        sh """
                            # Wait for application to start
                            sleep 30
                            
                            # Health check
                            ssh -o StrictHostKeyChecking=no ubuntu@${DEPLOYMENT_SERVER} '
                                curl -f http://localhost:8080/actuator/health || exit 1
                                echo "✅ Application health check passed"
                            '
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            // Update build description
            script {
                currentBuild.description = "Build: ${currentBuild.currentResult}"
            }
        }
        success {
            emailext (
                subject: "SUCCESS: Hotel Booking System Build #${env.BUILD_NUMBER}",
                body: """
                <h2>🚀 Build Successful!</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Commit:</b> ${env.GIT_COMMIT.substring(0,7)}</p>
                <p><b>Docker Image:</b> ${ECR_REPO}/${DOCKER_IMAGE}:${DOCKER_TAG}</p>
                <p><b>Deployed to:</b> ${DEPLOYMENT_SERVER}:8080</p>
                <p><b>View Build:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                <p><b>Application URL:</b> <a href="http://${DEPLOYMENT_SERVER}:8080">http://${DEPLOYMENT_SERVER}:8080</a></p>
                <p><b>SonarQube Report:</b> <a href="${SONAR_URL}/dashboard?id=hotel-booking-system">View Quality Gate</a></p>
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
                <h2>❌ Build Failed!</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Commit:</b> ${env.GIT_COMMIT.substring(0,7)}</p>
                <p><b>View Build:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                <p><b>Console Output:</b> <a href="${env.BUILD_URL}/console">View Logs</a></p>
                """,
                to: "mesaifudheenpv@gmail.com",
                from: "mesaifudheenpv@gmail.com",
                replyTo: "mesaifudheenpv@gmail.com"
            )
        }
        unstable {
            emailext (
                subject: "UNSTABLE: Hotel Booking System Build #${env.BUILD_NUMBER}",
                body: """
                <h2>⚠️ Build Unstable!</h2>
                <p><b>Project:</b> Hotel Booking System</p>
                <p><b>Build Number:</b> ${env.BUILD_NUMBER}</p>
                <p><b>Commit:</b> ${env.GIT_COMMIT.substring(0,7)}</p>
                <p><b>View Build:</b> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                <p>Usually due to test failures or quality gate issues.</p>
                """,
                to: "mesaifudheenpv@gmail.com",
                from: "mesaifudheenpv@gmail.com",
                replyTo: "mesaifudheenpv@gmail.com"
            )
        }
    }
}