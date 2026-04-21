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
            }
        }

        stage('Security: Gitleaks Scan') {
            steps {
                // Kiểm tra secret trong code
                sh 'gitleaks detect --source . -v || echo "Secrets detected OR Gitleaks error"'
            }
        }

        stage('Security: Snyk Security Scan') {
            steps {
                // Quét lỗ hổng dependency
                sh 'snyk test --all-projects || true'
            }
        }

        stage('Build Services (Conditional)') {
            parallel {
                stage('Build: Media Service') {
                    when { changeset "media/**" }
                    steps {
                        script { buildService("media") }
                    }
                }
                stage('Build: Product Service') {
                    when { changeset "product/**" } // Lưu ý: xem tên folder thực tế là catalog hay product
                    steps {
                        script { buildService("product") }
                    }
                }
                stage('Build: Cart Service') {
                    when { changeset "cart/**" }
                    steps {
                        script { buildService("cart") }
                    }
                }
                // Thêm stage cho các service khác nếu cần
            }
        }
    }

    post {
        always {
            // Upload kết quả test lên UI Jenkins
            junit '**/target/surefire-reports/*.xml'
        }
    }
}

// Hàm bổ trợ để build và check coverage
def buildService(serviceName) {
    dir(serviceName) {
        echo "Building ${serviceName}..."
        sh 'mvn clean test jacoco:report'
        
        // Logic kiểm tra Coverage > 70% từ file jacoco.csv hoặc xml
        // Đây là lệnh shell đơn giản để check con số tổng (line coverage)
        sh """
            COVERAGE=\$(grep -oE "Total.*?%" target/site/jacoco/index.html | head -1 | grep -oE "[0-9]+")
            echo "Current Coverage: \$COVERAGE%"
            if [ "\$COVERAGE" -lt 70 ]; then
                echo "FAILED: Coverage is below 70%"
                exit 1
            fi
        """
    }
}
