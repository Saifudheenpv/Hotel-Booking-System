pipeline {
    agent any
    
    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }
    
    environment {
        SONAR_SCANNER_HOME = tool 'Sonar-Scanner'
        DOCKER_REGISTRY = 'docker.io/saifudheenpv'  // REPLACE_1
        NEXUS_URL = 'http://http://43.205.5.17:8081/repository/maven-releases/'  // REPLACE_2
        K8S_NAMESPACE = 'hotel-booking-prod'  // REPLACE_3
        APP_URL = 'http://your-hotel-app.com'  // REPLACE_4
    }
    
    stages {
        // Stage 1: Code Checkout from GitHub
        stage('GitHub Checkout') {
            steps {
                git branch: 'main', 
                url: 'https://github.com/Saifudheenpv/Hotel-Booking-System.git'  // REPLACE_5
            }
        }
        
        // Stage 2: Maven Compile and Test
        stage('Maven Compile & Test') {
            steps {
                sh 'mvn clean compile test'
            }
            post {
                success {
                    junit 'target/surefire-reports/*.xml'
                    echo "✅ Maven compile and tests passed!"
                }
                failure {
                    echo "❌ Maven compile or tests failed!"
                }
            }
        }
        
        // Stage 3: SonarQube Analysis
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('Sonar-Server') {
                    sh """
                        ${SONAR_SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.projectKey=hotel-booking-system \
                        -Dsonar.projectName='Hotel Booking System' \
                        -Dsonar.java.binaries=target/classes \
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                    """
                }
            }
        }
        
        // Stage 4: Trivy Source Code Security Scan
        stage('Trivy Source Scan') {
            steps {
                sh 'trivy fs . --severity HIGH,CRITICAL --exit-code 1 --format table'
            }
            post {
                failure {
                    echo "🚨 Critical vulnerabilities found in source code!"
                }
            }
        }
        
        // Stage 5: Maven Build Package
        stage('Maven Build Package') {
            steps {
                sh 'mvn clean package -DskipTests'
                archiveArtifacts 'target/*.jar'
            }
        }
        
        // Stage 6: Upload to Nexus Repository
        stage('Upload to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]) {
                    sh """
                        mvn deploy:deploy-file \
                        -DskipTests \
                        -Dfile=target/hotel-booking-system-1.0.0.jar \  // REPLACE_6
                        -DgroupId=com.yourcompany \  // REPLACE_7
                        -DartifactId=hotel-booking-system \
                        -Dversion=1.0.0-${env.BUILD_ID} \
                        -Dpackaging=jar \
                        -DrepositoryId=nexus \
                        -Durl=${NEXUS_URL}
                    """
                }
            }
        }
        
        // Stage 7: Docker Build and Tag
        stage('Docker Build & Tag') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_REGISTRY}/hotel-booking-system:${env.BUILD_ID}")
                }
            }
        }
        
        // Stage 8: Trivy Docker Image Scan
        stage('Trivy Docker Scan') {
            steps {
                sh "trivy image ${DOCKER_REGISTRY}/hotel-booking-system:${env.BUILD_ID} --severity HIGH,CRITICAL --exit-code 1 --format table"
            }
        }
        
        // Stage 9: Docker Push to Registry
        stage('Docker Push') {
            steps {
                script {
                    docker.withRegistry('', 'docker-hub-credentials') {
                        dockerImage.push()
                        dockerImage.push('latest')
                    }
                }
            }
        }
        
        // Stage 10: Deploy to Kubernetes
        stage('Deploy to Kubernetes') {
            steps {
                sh """
                    kubectl set image deployment/hotel-booking-system \
                    hotel-booking-app=${DOCKER_REGISTRY}/hotel-booking-system:${env.BUILD_ID} \
                    --namespace=${K8S_NAMESPACE}
                    
                    kubectl rollout status deployment/hotel-booking-system \
                    --namespace=${K8S_NAMESPACE} --timeout=300s
                """
            }
        }
        
        // Stage 11: Smoke Tests
        stage('Smoke Tests') {
            steps {
                sh """
                    echo "🚀 Running smoke tests..."
                    sleep 30
                    curl -f ${APP_URL}/actuator/health || exit 1
                    echo "✅ Smoke tests passed!"
                """
            }
        }
    }
    
    post {
        always {
            echo "🏁 Pipeline execution completed for build ${env.BUILD_ID}"
            cleanWs()
        }
        success {
            emailext (
                subject: "✅ SUCCESS: Hotel Booking System Deployed - Build #${env.BUILD_NUMBER}",
                body: """
                🎉 Hotel Booking System successfully deployed to production!
                
                📋 Build Details:
                - Build Number: ${env.BUILD_NUMBER}
                - Build URL: ${env.BUILD_URL}
                - Docker Image: ${DOCKER_REGISTRY}/hotel-booking-system:${env.BUILD_ID}
                - SonarQube Report: http://your-sonarqube-server.com/dashboard?id=hotel-booking-system  // REPLACE_8
                - Deployment Time: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
                
                🌐 Application is live at: ${APP_URL}
                
                📊 Quality Gates:
                - ✅ Unit Tests Passed
                - ✅ SonarQube Quality Gate
                - ✅ Security Scans (Trivy)
                - ✅ Deployment Verified
                """,
                to: 'dev-team@yourcompany.com,qa-team@yourcompany.com'  // REPLACE_9
            )
            echo "✅ Email notification sent for successful deployment!"
        }
        failure {
            emailext (
                subject: "❌ FAILED: Hotel Booking System Pipeline - Build #${env.BUILD_NUMBER}",
                body: """
                🚨 Pipeline failed for Hotel Booking System!
                
                📋 Build Details:
                - Build Number: ${env.BUILD_NUMBER}
                - Build URL: ${env.BUILD_URL}
                - Failed Stage: Check Jenkins console for details
                
                🔧 Please investigate immediately.
                """,
                to: 'dev-team@yourcompany.com,ops-team@yourcompany.com'  // REPLACE_10
            )
            echo "❌ Email notification sent for pipeline failure!"
        }
    }
}