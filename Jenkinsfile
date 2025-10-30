pipeline {
    agent any
    
    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }
    
    environment {
        DOCKER_REGISTRY = 'docker.io/saifudheenpv'
        K8S_NAMESPACE = 'hotel-booking-prod'
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', 
                url: 'https://github.com/Saifudheenpv/Hotel-Booking-System.git'
            }
        }
        
        stage('Build & Test') {
            steps {
                sh 'mvn clean compile test'
            }
            post {
                success {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                sh 'trivy fs . --severity HIGH,CRITICAL --exit-code 1'
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    dockerImage = docker.build("${DOCKER_REGISTRY}/hotel-booking-system:${env.BUILD_ID}")
                }
            }
        }
        
        stage('Scan Docker Image') {
            steps {
                sh "trivy image ${DOCKER_REGISTRY}/hotel-booking-system:${env.BUILD_ID} --severity HIGH,CRITICAL --exit-code 1"
            }
        }
        
        stage('Push to Docker Hub') {
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
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo "✅ Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed!"
        }
    }
}