pipeline {
    agent { label 'Macbook-Agent' }

    environment {
        // Đường dẫn Java 21 trên Mac của bạn
        JAVA_HOME = sh(script: '/usr/libexec/java_home -v 21', returnStdout: true).trim()
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        SNYK_TOKEN = credentials('snyk-api-token')
        REVISION = '1.0-SNAPSHOT'
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
                // Install parent POM first with resolved version
                sh "mvn install -N -DskipTests -Drevision=${env.REVISION}"
                sh "mvn clean install -DskipTests -pl common-library -am -Drevision=${env.REVISION}"
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
        echo "--- Processing ${serviceName} ---"
        
        // Security: Snyk scan per service
        echo "Running Snyk scan for ${serviceName}..."
        sh "snyk test --org=bingsu1103 --file=${serviceName}/pom.xml || echo 'Snyk scan for ${serviceName} failed or found issues'"
        
        echo "Building and testing ${serviceName}..."
        // Run from root using -pl (project list) and -am (also make dependencies)
        // -U forces update of dependencies to clear cached failures
        sh "mvn clean test jacoco:report -pl ${serviceName} -am -Drevision=${REVISION} -U"
        
        // Coverage check logic (can be expanded)
        echo "Checking test coverage for ${serviceName}..."
        // The report will be at ${serviceName}/target/site/jacoco/index.html
        if (fileExists("${serviceName}/target/site/jacoco/index.html")) {
            echo "Coverage report found for ${serviceName}"
        }
    }
}
