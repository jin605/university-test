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

              - name: kubectl
                image: alpine/k8s:1.34.0
                command:
                - cat
                tty: true

              volumes:
              - name: docker-socket
                hostPath:
                  path: "/var/run/docker.sock"
            '''
        }
    }

    environment {
        // Docker Hub과 관련된 환경 변수
        DOCKER_IMAGE_NAME = "jin604/department-test"
        DOCKER_CREDENTIALS_ID = "dockerhub-access"
        DISCORD_WEBHOOK_CREDENTIALS_ID = "discord-webhook"

        // 매니페스트 저장소와 관련된 환경 변수
        MANIFEST_REPO_CREDENTIALS_ID = "github-jenkins-for-university-test-manifest"
        MANIFEST_REPO_URL = "git@github.com:jin605/university-test-manifest.git"

        // 상태 추적을 위한 환경 변수
        ARGOCD_APP_NAME = "university-test"
        ARGOCD_NAMESPACE = "argocd"
        APP_NAMESPACE = "university"
        APP_DEPLOYMENT_NAME = "department-api-deploy"

    }

    stages {

        stage('Init Status') {
            steps {
                script {
                    env.CI_STATUS = "NOT_STARTED"
                    env.MANIFEST_STATUS = "NOT_STARTED"
                    env.CD_STATUS = "NOT_STARTED"
                }
            }
        }        
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
                                env.CI_STATUS = "SUCCESS"

                            }
                        }                        
                    }
                }
            }
        }

        stage('Update Manifest Repository') {
            steps {
                container('docker') {
                    withCredentials([sshUserPrivateKey(
                        credentialsId: MANIFEST_REPO_CREDENTIALS_ID,
                        keyFileVariable: 'SSH_KEY',
                        usernameVariable: 'GIT_USERNAME'
                    )]) {
                        sh '''
                            apk add --no-cache git openssh-client

                            mkdir -p ~/.ssh
                            ssh-keyscan github.com >> ~/.ssh/known_hosts

                            rm -rf university-test-manifest

                            GIT_SSH_COMMAND="ssh -i $SSH_KEY -o UserKnownHostsFile=$HOME/.ssh/known_hosts" \
                            git clone "$MANIFEST_REPO_URL"

                            cd university-test-manifest

                            git config user.name "jenkins"
                            git config user.email "jenkins@local"

                            sed -i "s|image: jin604/department-test:.*|image: jin604/department-test:${BUILD_NUMBER}|g" university-test/department-api-deploy.yaml

                            git add university-test/department-api-deploy.yaml
                            git commit -m "Update department image to ${BUILD_NUMBER}" || echo "No changes to commit"

                            GIT_SSH_COMMAND="ssh -i $SSH_KEY -o UserKnownHostsFile=$HOME/.ssh/known_hosts" \
                            git push origin main
                        '''
                        script {
                            env.MANIFEST_STATUS = "SUCCESS"
                        }
                    }
                }
            }
        }

        stage('Verify CD Deployment') {
            steps {
                container('kubectl') {
                    script {
                        env.CD_STATUS = "CHECKING"

                        try {
                            def expectedImage = "${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}"

                                sh """
                                    echo "Expected image: ${expectedImage}"

                                    for i in \$(seq 1 30); do
                                    CURRENT_IMAGE=\$(kubectl get deploy ${APP_DEPLOYMENT_NAME} \
                                        -n ${APP_NAMESPACE} \
                                        -o=jsonpath='{.spec.template.spec.containers[0].image}')

                                    echo "Current image: \$CURRENT_IMAGE"

                                    if [ "\$CURRENT_IMAGE" = "${expectedImage}" ]; then
                                        break
                                    fi

                                    echo "Waiting for Argo CD to apply new image..."
                                    sleep 10
                                    done

                                    kubectl rollout status deployment/${APP_DEPLOYMENT_NAME} \
                                    -n ${APP_NAMESPACE} \
                                    --timeout=300s

                                    CURRENT_IMAGE=\$(kubectl get deploy ${APP_DEPLOYMENT_NAME} \
                                    -n ${APP_NAMESPACE} \
                                    -o=jsonpath='{.spec.template.spec.containers[0].image}')

                                    echo "Current image after rollout: \$CURRENT_IMAGE"

                                    if [ "\$CURRENT_IMAGE" != "${expectedImage}" ]; then
                                    echo "CD failed: expected ${expectedImage}, but got \$CURRENT_IMAGE"
                                    exit 1
                                    fi

                                    APP_SYNC=\$(kubectl get application ${ARGOCD_APP_NAME} \
                                    -n ${ARGOCD_NAMESPACE} \
                                    -o=jsonpath='{.status.sync.status}')

                                    APP_HEALTH=\$(kubectl get application ${ARGOCD_APP_NAME} \
                                    -n ${ARGOCD_NAMESPACE} \
                                    -o=jsonpath='{.status.health.status}')

                                    echo "Argo CD Sync: \$APP_SYNC"
                                    echo "Argo CD Health: \$APP_HEALTH"

                                    if [ "\$APP_SYNC" != "Synced" ] || [ "\$APP_HEALTH" != "Healthy" ]; then
                                    echo "CD failed: Argo CD is \$APP_SYNC / \$APP_HEALTH"
                                    exit 1
                                    fi
                                """

                            env.CD_STATUS = "SUCCESS"
                        } catch (err) {
                            env.CD_STATUS = "FAILED"
                            throw err
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
                    "content": "✅ **CI/CD Success**\\n\\n**Job**: ${env.JOB_NAME}\\n**Build**: #${env.BUILD_NUMBER}\\n**CI**: ${env.CI_STATUS}\\n**Manifest**: ${env.MANIFEST_STATUS}\\n**CD**: ${env.CD_STATUS}\\n**Image**: ${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}\\n**Argo CD App**: ${ARGOCD_APP_NAME}\\n**Namespace**: ${APP_NAMESPACE}\\n**Duration**: ${currentBuild.durationString}\\n**URL**: ${env.BUILD_URL}"
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
                    "content": "❌ **CI/CD Failed**\\n\\n**Job**: ${env.JOB_NAME}\\n**Build**: #${env.BUILD_NUMBER}\\n**CI**: ${env.CI_STATUS}\\n**Manifest**: ${env.MANIFEST_STATUS}\\n**CD**: ${env.CD_STATUS}\\n**Image**: ${DOCKER_IMAGE_NAME}:${env.BUILD_NUMBER}\\n**Stage**: Check Jenkins Console Log\\n**Duration**: ${currentBuild.durationString}\\n**URL**: ${env.BUILD_URL}"
                }' \
                "${DISCORD_WEBHOOK_URL}"
                """
            }
        }
    }

}