pipeline {
    agent none

    stages {
        stage('Build') {
            agent { label 'linux-jdk21' }

            steps {
                sh 'chmod +x gradlew'
                sh './gradlew build'
                script {
                    def sanitizedBranch = env.BRANCH_NAME.replaceAll(/[^a-zA-Z0-9._]/, '_')
                    def shortHash = env.GIT_COMMIT.substring(0, 6)
                    def jars = findFiles(glob: 'build/libs/Garden*.jar')

                    jars.each { jar ->
                        def newPath = jar.path.replaceFirst(/\.jar$/, "-${sanitizedBranch}.jar")
                        sh "mv '${jar.path}' '${newPath}'"
                    }
                }
            }

            post {
                always {
                    archiveArtifacts artifacts: 'build/libs/Garden*.jar', fingerprint: true
                }
            }
        }
    }
}