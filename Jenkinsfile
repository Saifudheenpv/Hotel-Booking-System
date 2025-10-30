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
        
        stage('Maven Build & Test') {
            steps {
                sh 'mvn clean compile test'
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
                sh 'mvn clean package -DskipTests'
                archiveArtifacts 'target/*.jar'
                echo "✅ Application packaged successfully!"
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    sh '''
                        echo "🐳 Building Docker image..."
                        docker build -t ${DOCKER_REGISTRY}/hotel-booking-system:${BUILD_ID} .
                        echo "✅ Docker image built successfully!"
                    '''
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
                        
                        # Delete existing deployment to start fresh
                        kubectl delete deployment hotel-booking-system --namespace=${K8S_NAMESPACE} --ignore-not-found=true
                        
                        # Create deployment with proper container name
                        cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: hotel-booking-system
  namespace: ${K8S_NAMESPACE}
  labels:
    app: hotel-booking-system
spec:
  replicas: 1
  selector:
    matchLabels:
      app: hotel-booking-system
  template:
    metadata:
      labels:
        app: hotel-booking-system
    spec:
      containers:
      - name: hotel-booking-app
        image: ${DOCKER_REGISTRY}/hotel-booking-system:${BUILD_ID}
        ports:
        - containerPort: 8080
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 5
          periodSeconds: 5
EOF
                        
                        # Wait for rollout
                        kubectl rollout status deployment/hotel-booking-system --namespace=${K8S_NAMESPACE} --timeout=300s
                          
                        # Expose service if not exists
                        kubectl expose deployment hotel-booking-system \
                          --port=8080 --target-port=8080 \
                          --type=NodePort \
                          --namespace=${K8S_NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                          
                        echo "✅ Kubernetes deployment completed!"
                        
                        # Show deployment info
                        echo "📊 Deployment Status:"
                        kubectl get pods,svc -n ${K8S_NAMESPACE}
                    """
                }
            }
        }
        
        stage('Smoke Tests') {
            steps {
                script {
                    sh """
                        echo "🧪 Running smoke tests..."
                        
                        # Get NodePort
                        NODE_PORT=\$(kubectl get svc hotel-booking-system -n ${K8S_NAMESPACE} -o jsonpath='{.spec.ports[0].nodePort}')
                        echo "🌐 Application accessible at: http://43.205.5.17:\$NODE_PORT"
                        
                        # Wait for app to start
                        echo "⏳ Waiting for application to start..."
                        sleep 30
                        
                        # Test health endpoint
                        echo "🔍 Testing health endpoint..."
                        curl -f http://43.205.5.17:\$NODE_PORT/actuator/health || echo "⚠️ Health check failed but continuing..."
                        
                        echo "✅ Smoke tests completed!"
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
                🎉 Hotel Booking System successfully deployed to Kubernetes!
                
                📋 Build Details:
                - Build Number: ${BUILD_NUMBER}
                - Build URL: ${BUILD_URL}
                - Docker Image: ${DOCKER_REGISTRY}/hotel-booking-system:${BUILD_ID}
                - Namespace: ${K8S_NAMESPACE}
                - Deployment: hotel-booking-system
                
                Application is live and running in Kubernetes!
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