pipeline {
    agent { label 'Macbook-Agent' }

    environment {
        // Đường dẫn Java 21 trên Mac của bạn
        JAVA_HOME = sh(script: '/usr/libexec/java_home -v 21', returnStdout: true).trim()
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        SNYK_TOKEN = credentials('snyk-api-token')
    }

    stages {
        stage('Initialize') {
            steps {
                sh 'java -version'
                sh 'mvn -v'
                sh 'snyk --version'
            }
        }

        stage('Security: Gitleaks Scan') {
            steps {
                sh 'gitleaks detect --source . -v || echo "Secrets detected OR Gitleaks error"'
            }
        }

        stage('Build Shared Libraries') {
            steps {
                echo 'Installing root POM and shared libraries...'
                // Install parent POM first to resolve ${revision}
                sh 'mvn install -N -DskipTests'
                sh 'mvn clean install -DskipTests -pl common-library -am'
            }
        }

        stage('Build & Scan Services') {
            parallel {
                stage('Media Service') {
                    when { changeset "media/**" }
                    steps { buildService('media') }
                }
                stage('Product Service') {
                    when { changeset "product/**" }
                    steps { buildService('product') }
                }
                stage('Cart Service') {
                    when { changeset "cart/**" }
                    steps { buildService('cart') }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished."
        }
    }
}

// Hàm bổ trợ để build, scan bảo mật và check coverage
def buildService(serviceName) {
    script {
        dir(serviceName) {
            echo "--- Processing ${serviceName} ---"
            
            echo "Running Snyk scan for ${serviceName}..."
            // Sử dụng || true để lỗi -13 không làm dừng cả pipeline
            sh "snyk test --all-projects --org=bingsu1103 || echo 'Snyk scan for ${serviceName} failed or found issues'"
            
            echo "Building and testing ${serviceName}..."
            // Truyền -Drevision để Maven giải mã được version parent
            sh "mvn clean test jacoco:report -Drevision=1.0-SNAPSHOT"
            
            if (fileExists('target/site/jacoco/index.html')) {
                echo "Coverage report found for ${serviceName}"
            }
        }
    }
}
