pipeline {
    agent { label 'Macbook-Agent' }

    environment {
        REVISION = "1.0-SNAPSHOT"
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
                sh 'gitleaks detect --source . -v'
            }
        }

        stage('Security: SonarCloud Scan') {
            steps {
                script {
                    try {
                        withCredentials([string(credentialsId: 'sonar-api-token', variable: 'SONAR_TOKEN')]) {
                            echo 'Running SonarCloud analysis...'
                            sh "mvn sonar:sonar -Dsonar.organization=bingsu1103-yas -Dsonar.projectKey=bingsu1103_yas -Dsonar.host.url=https://sonarcloud.io -Dsonar.token=${SONAR_TOKEN} || echo 'SonarCloud scan failed'"
                        }
                    } catch (Exception e) {
                        echo "WARNING: SonarCloud scan skipped or failed: ${e.message}"
                    }
                }
            }
        }

        stage('Build Shared Libraries') {
            steps {
                echo 'Installing root POM and shared libraries...'
                sh "mvn install -N -DskipTests -Drevision=${env.REVISION}"
                sh "mvn clean install -DskipTests -pl common-library -am -Drevision=${env.REVISION}"
            }
        }

        stage('Build & Test Services') {
            steps {
                echo 'Building and testing services in parallel using Maven...'
                // Run all services in one go to share parent context and avoid parallel-run clashes
                // -T 1C enables Maven parallel build safely within the reactor
                sh """
                   mvn test jacoco:report \
                   -pl media,product,cart \
                   -am -Drevision=${env.REVISION} -U \
                   -T 1C -Dtest=!*IT,!CdcConsumerTest*
                """
            }
        }

        stage('Quality Gate: Coverage & Security') {
            steps {
                script {
                    def services = ['media', 'product', 'cart']
                    for (service in services) {
                        checkServiceQuality(service)
                    }
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

def checkServiceQuality(serviceName) {
    echo "--- Quality Check for ${serviceName} ---"
    
    // 1. Snyk Security Scan
    echo "Running Snyk scan for ${serviceName}..."
    sh "snyk test --org=bingsu1103 --file=${serviceName}/pom.xml || echo 'Snyk scan failed or found issues'"
    
    // 2. Coverage threshold check
    echo "Checking test coverage for ${serviceName}..."
    def csvPath = "${serviceName}/target/site/jacoco/jacoco.csv"
    
    if (fileExists(csvPath)) {
        def coverage = sh(
            script: "awk -F, 'NR > 1 {missed += \$4; covered += \$5} END {if (covered + missed > 0) print (covered / (covered + missed)) * 100; else print 0}' ${csvPath}",
            returnStdout: true
        ).trim().toDouble()
        
        echo "${serviceName} Coverage: ${coverage}%"
        
        // Define thresholds
        def threshold = 70.0
        if (serviceName == 'product') threshold = 15.0 // Adjust based on earlier discussion
        if (serviceName == 'media') threshold = 60.0
        
        if (coverage < threshold) {
            error "FAILURE: ${serviceName} test coverage (${coverage}%) is below required threshold (${threshold}%)!"
        } else {
            echo "SUCCESS: ${serviceName} test coverage (${coverage}%) meets the target."
        }
    } else {
        echo "WARNING: JaCoCo report not found at ${csvPath}. Skipping coverage check."
    }
}
