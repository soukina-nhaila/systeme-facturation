
pipeline {
    agent any

    tools {
        maven 'maven-3.9.9'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/soukina-nhaila/systeme-facturation.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B clean package -DskipTests'
            }
        }

        stage('OWASP Dependency-Check') {
            steps {
                withCredentials([
                    string(
                        credentialsId: 'nvd-api-key',
                        variable: 'NVD_API_KEY'
                    )
                ]) {
                    sh '''
                        mvn -B org.owasp:dependency-check-maven:13.0.0:check \
                          -DnvdApiKeyEnvironmentVariable=NVD_API_KEY \
                          -DsuppressionFiles=config/dependency-check-suppressions.xml
                    '''
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts(
                artifacts: 'target/dependency-check-report.html,target/*.war',
                allowEmptyArchive: true
            )
        }
    }
}
