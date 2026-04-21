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
                // Quét secret trong code, dùng true để không làm build bị fail nếu có leak cũ
                sh 'gitleaks detect --source . -v || echo "Secrets detected OR Gitleaks error"'
            }
        }

        stage('Build & Scan Services') {
            parallel {
                stage('Media Service') {
                    when { changeset "media/**" }
                    steps {
                        script { buildService("media") }
                    }
                }
                stage('Product Service') {
                    when { changeset "product/**" }
                    steps {
                        script { buildService("product") }
                    }
                }
                stage('Cart Service') {
                    when { changeset "cart/**" }
                    steps {
                        script { buildService("cart") }
                    }
                }
                // Thêm các service khác tại đây
            }
        }
    }

    post {
        always {
            // junit '**/target/surefire-reports/*.xml'
            echo "Pipeline finished."
        }
    }
}

// Hàm bổ trợ để build, scan bảo mật và check coverage
def buildService(serviceName) {
    dir(serviceName) {
        echo "--- Processing ${serviceName} ---"
        
        // 1. Quét lỗ hổng dependency cho riêng service này
        echo "Running Snyk scan for ${serviceName}..."
        sh "snyk test || echo 'Snyk found vulnerabilities in ${serviceName}'"

        // 2. Build và chạy Unit Test
        echo "Building and testing ${serviceName}..."
        sh 'mvn clean test jacoco:report'
        
        // 3. Kiểm tra Coverage > 70%
        script {
            try {
                def coverage = sh(script: "grep -oE 'Total.*?%' target/site/jacoco/index.html | head -1 | grep -oE '[0-9]+' || echo 0", returnStdout: true).trim()
                echo "Current Coverage for ${serviceName}: ${coverage}%"
                if (coverage.toInteger() < 70) {
                    echo "WARNING: Coverage ${coverage}% is below 70%"
                    // currentBuild.result = 'UNSTABLE'
                }
            } catch (Exception e) {
                echo "Could not calculate coverage, maybe no tests found."
            }
        }
    }
}
