pipeline {
    agent any

    environment {
        SERVICES = 'api-gateway user-service game-data-service lobby-service battle-service rating-service'
        BUILD_TAG = "${env.BUILD_NUMBER ?: 'local'}"
    }

    stages {
        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests -q'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -q'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                script {
                    def services = env.SERVICES.split(' ')
                    services.each { svc ->
                        sh "docker build -f ${svc}/Dockerfile -t ${svc}:${BUILD_TAG} -t ${svc}:latest ."
                    }
                }
            }
        }

        stage('Minikube Load') {
            steps {
                script {
                    def services = env.SERVICES.split(' ')
                    services.each { svc ->
                        sh "minikube image load ${svc}:${BUILD_TAG}"
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                sh 'kubectl apply -f infra/k8s/'
                sh 'kubectl rollout status deployment/api-gateway -n poke-battle --timeout=120s'
                sh 'kubectl rollout status deployment/user-service -n poke-battle --timeout=120s'
                sh 'kubectl rollout status deployment/game-data-service -n poke-battle --timeout=180s'
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded — all services deployed to minikube"
        }
        failure {
            echo "Pipeline failed — check logs above"
        }
    }
}
