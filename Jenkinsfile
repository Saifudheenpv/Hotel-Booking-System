pipeline {
    agent any
    
    tools {
        jdk 'JDK21'
        maven 'Maven3'
    }
    
    environment {
        SONAR_SCANNER_HOME = tool 'Sonar-Scanner'
        DOCKER_REGISTRY = 'docker.io/saifudheenpv'
        NEXUS_URL = 'http://43.205.5.17:8081/repository/maven-releases/'
        K8S_NAMESPACE = 'hotel-booking-prod'
        APP_URL = 'http://43.205.5.17:8080'  // Using your server IP
    }
    
    stages {
        stage('GitHub Checkout') {
            steps {
                git branch: 'main', 
                url: 'https://github.com/Saifudheenpv/Hotel-Booking-System.git'
            }
        }
        
        stage('Maven Compile & Test') {
            steps {
                sh 'mvn clean compile test'
            }
            post {
                success {
                    junit 'target/surefire-reports/*.xml'
                    echo "✅ Maven compile and tests passed!"
                }
            }
        }
        
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
        
        stage('Trivy Source Scan') {
            steps {
                sh 'trivy fs . --severity HIGH,CRITICAL --exit-code 1 --format table'
            }
        }
        
        stage('Maven Build Package') {
            steps {
                sh 'mvn clean package -DskipTests'
                archiveArtifacts 'target/*.jar'
            }
        }
        
        stage('Upload to Nexus') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'nexus-credentials', usernameVariable: 'NEXUS_USER', passwordVariable: 'NEXUS_PASSWORD')]) {
                    sh """
                        mvn deploy:deploy-file \
                        -DskipTests \
                        -Dfile=target/hotel-booking-system-0.0.1-SNAPSHOT.jar \  // USE ACTUAL JAR NAME
                        -DgroupId=com.example \  // USE ACTUAL GROUP ID FROM POM.XML
                        -DartifactId=hotel-booking-system \
                        -Dversion=1.0.0-${env.BUILD_ID} \
                        -Dpackaging=jar \
                        -DrepositoryId=nexus \
                        -Durl=${NEXUS_URL}
                    """
                }
            }
        }
        
        stage('Docker Build & Tag') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_REGISTRY}/hotel-booking-system:${env.BUILD_ID}")
                }
            }
        }
        
        stage('Trivy Docker Scan') {
            steps {
                sh "trivy image ${DOCKER_REGISTRY}/hotel-booking-system:${env.BUILD_ID} --severity HIGH,CRITICAL --exit-code 1 --format table"
            }
        }
        
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
                🎉 Hotel Booking System successfully deployed!
                
                📋 Build Details:
                - Build Number: ${env.BUILD_NUMBER}
                - Build URL: ${env.BUILD_URL}
                - Docker Image: ${DOCKER_REGISTRY}/hotel-booking-system:${env.BUILD_ID}
                - SonarQube Report: http://43.205.5.17:9000/dashboard?id=hotel-booking-system
                - Deployment Time: ${new Date().format('yyyy-MM-dd HH:mm:ss')}
                
                🌐 Application is live at: ${APP_URL}
                """,
                to: 'saifudheenpv@gmail.com'  // YOUR EMAIL
            )
        }
        failure {
            emailext (
                subject: "❌ FAILED: Hotel Booking System Pipeline - Build #${env.BUILD_NUMBER}",
                body: """
                🚨 Pipeline failed for Hotel Booking System!
                
                Build Number: ${env.BUILD_NUMBER}
                Build URL: ${env.BUILD_URL}
                """,
                to: 'saifudheenpv@gmail.com'  // YOUR EMAIL
            )
        }
    }
}