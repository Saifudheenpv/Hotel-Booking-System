pipeline {
    agent any
    
    tools {
        jdk 'JDK17'
        maven 'Maven3'
    }
    
    environment {
        DOCKER_REGISTRY = 'docker.io/saifudheenpv'
        K8S_NAMESPACE = 'hotel-booking-prod'
        APP_URL = 'http://43.205.5.17:8080'
    }
    
    stages {
        stage('GitHub Checkout') {
            steps {
                git branch: 'main', 
                url: 'https://github.com/Saifudheenpv/Hotel-Booking-System.git'
            }
        }
        
        stage('Verify Project Structure') {
            steps {
                sh '''
                    echo "📁 Project Structure:"
                    ls -la
                    echo "📦 Source Code:"
                    find src -name "*.java" | head -10
                    echo "🐳 Dockerfile:"
                    cat Dockerfile
                '''
            }
        }
        
        stage('Maven Build & Test') {
            steps {
                sh '''
                    echo "🔨 Running Maven build..."
                    mvn clean compile test
                    echo "📦 Checking build output..."
                    ls -la target/
                    find target/ -name "*.jar" -o -name "*.class" | head -10
                '''
            }
            post {
                success {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                    echo "✅ Build and tests completed!"
                }
            }
        }
        
        stage('Package Application') {
            steps {
                sh '''
                    echo "📦 Packaging application..."
                    mvn clean package -DskipTests
                    echo "📋 Build artifacts:"
                    ls -la target/
                    echo "📦 JAR files:"
                    find target/ -name "*.jar" -type f
                '''
            }
        }
        
        stage('Security Scan - Report Only') {
            steps {
                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh 'trivy fs . --severity HIGH,CRITICAL --format table --exit-code 0'
                    }
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh '''
                        echo "🐳 Building Docker image..."
                        # List all JAR files to find the correct one
                        echo "📦 Available JAR files:"
                        find target/ -name "*.jar" -type f
                        
                        # Build Docker image
                        docker build -t ${DOCKER_REGISTRY}/hotel-booking-system:${BUILD_ID} .
                        echo "✅ Docker image built successfully!"
                    '''
                }
            }
        }
        
        stage('Scan Docker Image - Report Only') {
            steps {
                script {
                    catchError(buildResult: 'SUCCESS', stageResult: 'UNSTABLE') {
                        sh "trivy image ${DOCKER_REGISTRY}/hotel-booking-system:${BUILD_ID} --severity HIGH,CRITICAL --format table --exit-code 0"
                    }
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('', 'dockerhub-creds') {
                        sh """
                            docker push ${DOCKER_REGISTRY}/hotel-booking-system:${BUILD_ID}
                            docker tag ${DOCKER_REGISTRY}/hotel-booking-system:${BUILD_ID} ${DOCKER_REGISTRY}/hotel-booking-system:latest
                            docker push ${DOCKER_REGISTRY}/hotel-booking-system:latest
                        """
                    }
                    echo "✅ Docker image pushed to registry!"
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh """
                        echo "🚀 Deploying to Kubernetes..."
                        
                        # Create namespace if not exists
                        kubectl create namespace ${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                        
                        # Create deployment if not exists
                        kubectl create deployment hotel-booking-system \
                          --image=${DOCKER_REGISTRY}/hotel-booking-system:latest \
                          --namespace=${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                          
                        # Update image
                        kubectl set image deployment/hotel-booking-system \
                          hotel-booking-app=${DOCKER_REGISTRY}/hotel-booking-system:${BUILD_ID} \
                          --namespace=${K8S_NAMESPACE}
                          
                        # Wait for rollout
                        kubectl rollout status deployment/hotel-booking-system \
                          --namespace=${K8S_NAMESPACE} --timeout=300s
                          
                        # Expose service if not exists
                        kubectl expose deployment hotel-booking-system \
                          --port=8080 --target-port=8080 \
                          --type=NodePort \
                          --namespace=${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                          
                        echo "✅ Kubernetes deployment completed!"
                        
                        # Show deployment info
                        kubectl get pods,svc -n ${K8S_NAMESPACE}
                    """
                }
            }
        }
    }
    
    post {
        always {
            echo "🏁 Pipeline execution completed for build ${BUILD_ID}"
            cleanWs()
        }
        success {
            emailext (
                subject: "✅ SUCCESS: Hotel Booking System Deployed - Build #${BUILD_NUMBER}",
                body: """
                🎉 Hotel Booking System successfully deployed!
                
                Build Number: ${BUILD_NUMBER}
                Build URL: ${BUILD_URL}
                Docker Image: ${DOCKER_REGISTRY}/hotel-booking-system:${BUILD_ID}
                """,
                to: 'saifudheenpv@gmail.com'
            )
        }
        failure {
            emailext (
                subject: "❌ FAILED: Hotel Booking System Pipeline - Build #${BUILD_NUMBER}",
                body: """
                🚨 Pipeline failed for Hotel Booking System!
                
                Build Number: ${BUILD_NUMBER}
                Build URL: ${BUILD_URL}
                """,
                to: 'saifudheenpv@gmail.com'
            )
        }
    }
}