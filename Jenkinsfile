pipeline {
    agent {
        kubernetes {
            yaml '''
            apiVersion: v1
            kind: Pod
            metadata:
              name: jenkins-agent
            spec:
              containers:
              - name: maven
                image: maven:3.9.15-eclipse-temurin-21-alpine
                command:
                - cat
                tty: true
              - name: docker
                image: docker:29.4.1-cli-alpine3.23
                command:
                - cat
                tty: true
                volumeMounts:
                - mountPath: "/var/run/docker.sock"
                  name: docker-socket
              volumes:
              - name: docker-socket
                hostPath:
                  path: "/var/run/docker.sock"
            '''
        }
    }

    environment {
        DOCKER_IMAGE_NAME = "jin604/department-test"
        DOCKER_CREDENTIALS_ID = "dockerhub-access"
        DISCORD_WEBHOOK_CREDENTIALS_ID = "discord-webhook"
        
    }

    stages {
        // stage('SonarQube Analysis') {
        //     steps {
        //         container('maven') {
        //             withSonarQubeEnv('sonarqube-server') {
        //                 sh """mvn verify sonar:sonar \
        //                     -Dsonar.projectKey='DepartmentService' \
        //                     -Dsonar.projectName='DepartmentService'"""
        //             }  
        //         }
        //     }
        // }

        stage('Maven Build') {
            steps {
                container('maven') {
                    dir('BE_DepartmentService') {

                        sh 'pwd'
                        sh 'ls -al'
                        sh 'mvn -v'
                        // sh 'mvn clean'
                        sh 'mvn package -DskipTests'
                        sh 'ls -al'
                        sh 'ls -al ./target'
                    }
                }
            }
        }

        stage('Docker Image Build & Push') {
            steps {
                container('docker') {
                    dir('BE_DepartmentService') {

                        script {
                            def buildNumber = "${env.BUILD_NUMBER}"

                            sh 'docker logout'

                            // withCredentials()
                            //  - 파이프라인에서 자격 증명을 사용할 수 있는 블록을 생성한다. 
                            // usernamePassword()
                            //  - 자격 증명 중 사용자 이름과 비밀번호를 가져온다.
                            //  - credentialsId는 자격 증명을 식별할 수 있는 식별자를 작성한다.
                            //  - usernameVariable은 자격 증명에서 가져온 사용자 이름을 저장하는 환경 변수의 이름을 작성한다.
                            //  - passwordVariable은 자격 증명에서 가져온 비밀번호를 저장하는 환경 변수의 이름을 작성한다.
                            withCredentials([usernamePassword(
                                credentialsId: DOCKER_CREDENTIALS_ID,
                                usernameVariable: 'DOCKER_USERNAME',
                                passwordVariable: 'DOCKER_PASSWORD'
                            )]) {
                                sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                            }

                            // 파이프라인 단계에서 환경 변수를 설정하는 역할을 한다.
                            withEnv(["DOCKER_IMAGE_VERSION=${buildNumber}"]) {
                                sh 'docker -v'
                                sh 'echo $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION'
                                sh 'docker build --no-cache -t $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION ./'
                                sh 'docker image inspect $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION'
                                sh 'docker push $DOCKER_IMAGE_NAME:$DOCKER_IMAGE_VERSION'

                            }
                        }                        
                    }
                }
            }
        }
    }

    // post {
    //     always {
    //         withCredentials([string(
    //             credentialsId: DISCORD_WEBHOOK_CREDENTIALS_ID,
    //             variable: 'DISCORD_WEBHOOK_URL'
    //         )]) {
    //             timeout(time: 10, unit: 'SECONDS') {
    //                 discordSend description: """
    //                 제목 : ${env.JOB_NAME}:${currentBuild.displayName}
    //                 결과 : ${currentBuild.result}
    //                 실행 시간 : ${currentBuild.duration / 1000}s
    //                 """,
    //                 result: currentBuild.currentResult,
    //                 title: "${env.JOB_NAME}", 
    //                 webhookURL: "${DISCORD_WEBHOOK_URL}"
    //             }
    //         }
    //     }
    //     success {
    //         // string()
    //         //   - 자격 증명 중 시크릿 텍스트를 가져온다.
    //         withCredentials([string(
    //             credentialsId: DISCORD_WEBHOOK_CREDENTIALS_ID,
    //             variable: 'DISCORD_WEBHOOK_URL'
    //         )]) {
    //             timeout(time: 10, unit: 'SECONDS') {
    //                 discordSend description: """
    //                 제목 : ${currentBuild.displayName} 성공
    //                 결과 : ${currentBuild.result}
    //                 실행 시간 : ${currentBuild.duration / 1000}s
    //                 """,
    //                 result: currentBuild.currentResult,
    //                 title: "${env.JOB_NAME} : ${currentBuild.displayName}", 
    //                 webhookURL: "${DISCORD_WEBHOOK_URL}"
    //             }
    //         }
    //     }

    //     failure {
    //         withCredentials([string(
    //             credentialsId: DISCORD_WEBHOOK_CREDENTIALS_ID,
    //             variable: 'DISCORD_WEBHOOK_URL'
    //         )]) {
    //             timeout(time: 10, unit: 'SECONDS') {
    //                 discordSend description: """
    //                 제목 : ${currentBuild.displayName} 실패
    //                 결과 : ${currentBuild.result}
    //                 실행 시간 : ${currentBuild.duration / 1000}s
    //                 """,
    //                 result: currentBuild.currentResult,
    //                 title: "${env.JOB_NAME} : ${currentBuild.displayName}", 
    //                 webhookURL: "${DISCORD_WEBHOOK_URL}"
    //             }
    //         }
    //     }
    // }

    post {
        success {
            withCredentials([string(
                credentialsId: DISCORD_WEBHOOK_CREDENTIALS_ID,
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                sh """
                curl -X POST \
                -H 'Content-Type: application/json' \
                --data '{
                    "content": "✅ **Jenkins Build Success**\\n\\n**Job**: ${env.JOB_NAME}\\n**Build**: #${env.BUILD_NUMBER}\\n**Branch**: ${env.BRANCH_NAME ?: "N/A"}\\n**Image**: ${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}\\n**Duration**: ${currentBuild.durationString}\\n**URL**: ${env.BUILD_URL}"
                }' \
                "${DISCORD_WEBHOOK_URL}"
                """
            }
        }

        failure {
            withCredentials([string(
                credentialsId: DISCORD_WEBHOOK_CREDENTIALS_ID,
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                sh """
                curl -X POST \
                -H 'Content-Type: application/json' \
                --data '{
                    "content": "❌ **Jenkins Build Failed**\\n\\n**Job**: ${env.JOB_NAME}\\n**Build**: #${env.BUILD_NUMBER}\\n**Branch**: ${env.BRANCH_NAME ?: "N/A"}\\n**Stage**: Check Jenkins Console Log\\n**Duration**: ${currentBuild.durationString}\\n**URL**: ${env.BUILD_URL}"
                }' \
                "${DISCORD_WEBHOOK_URL}"
                """
            }
        }
    }
    
}