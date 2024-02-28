def incrementVersion() {
    echo "incrementing application version..."
    sh "mvn build-helper:parse-version versions:set \
        -DnewVersion=\\\${parsedVersion.majorVersion}.\\\${parsedVersion.minorVersion}.\\\${parsedVersion.nextIncrementalVersion} \
        versions:commit"
    def matcher = readFile("pom.xml") =~ "<version>(.+)</version>"
    def version = matcher[0][1]
    env.IMAGE_VERSION = "$version-$BUILD_NUMBER"
}

def testApp() {
    echo "testing the application..."
    sh "mvn clean package"
    sh "mvn test"
}

def buildApp() {
    echo "building the application jar..."
    sh "mvn package"
}

def buildImage() {
    echo "building the docker image..."
    env.IMAGE_NAME = "ismailsdockers/java-maven-app"
    sh "docker build -t $IMAGE_NAME:$IMAGE_VERSION ."
    sh "docker tag $IMAGE_NAME:$IMAGE_VERSION $IMAGE_NAME:latest"
}

def pushImage() {
    echo "pushing the docker image to docker private repository..."

    withCredentials([usernamePassword(
        credentialsId: 'docker-credentials',
        usernameVariable: 'USER',
        passwordVariable: 'PASSWORD'
    )]) {
        sh "echo $PASSWORD | docker login -u $USER --password-stdin"
    }

    sh "docker push $IMAGE_NAME:$IMAGE_VERSION"
    sh "docker push $IMAGE_NAME:latest"
}

def deploy() {
    echo "deploying docker image to AWS EKS..."
    
    withCredentials([aws(
        accessKeyVariable: 'AWS_ACCESS_KEY_ID',
        credentialsId: 'aws-credentials',
        secretKeyVariable: 'AWS_SECRET_ACCESS_KEY'
    )]) {
        sh "aws eks update-kubeconfig --name eks-cluster-1"
        sh "envsubst < kubernetes/deployment.yaml | kubectl apply -f -"
        sh "envsubst < kubernetes/service.yaml | kubectl apply -f -"
    }
}

def versionBump() {
    echo "committing verison update to git repository..."

    sh 'git config --global user.email "jenkins@example.com"'
    sh 'git config --global user.name "jenkins"'

    sh "git status"
    sh "git branch"

    withCredentials([usernamePassword(
        credentialsId: 'gitlab-credentials',
        usernameVariable: 'USER',
        passwordVariable: 'PASSWORD'
    )]) {
        sh "git remote set-url origin https://$USER:$PASSWORD@gitlab.com/ismailGitlab/ci-cd-pipeline-with-jenkins-eks-dockerhub.git"
    }

    sh "git add ."
    sh "git commit -m 'jenkins-ci: version bump'"
    sh "git push origin HEAD:main"
}

return this