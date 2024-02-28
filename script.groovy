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

def provisionServer() {
    echo "provisioning ec2 server..."
    dir("terraform") {
        sh "terraform init"
        sh "terraform apply --auto-approve"
        SERVER_PUBLIC_IP = sh(
            script: "terraform output ec2_public_ip"
            returnStdout: true
        ).trim()
    }
}

def deploy() {
    echo "waiting for EC2 server to initialize..."
    sleep(time: 90, unit: "SECONDS")

    echo "deploying docker image to AWS EC2 server..."
    echo "server_public_ip: $SERVER_PUBLIC_IP"
    
    def shellCmd = "bash ./server-commands.sh '$IMAGE_NAME:$IMAGE_VERSION'"
    def ec2Instance = "ec2-user@$SERVER_PUBLIC_IP"

    sshagent['myapp-server-ssh-key'] {
        ssh "scp -o StrictHostKeyChecking=no docker-compose.yaml $ec2Instance:/home/ec2-user"
        ssh "scp -o StrictHostKeyChecking=no server-commands.sh $ec2Instance:/home/ec2-user"
        ssh "ssh -o StrictHostKeyChecking=no $ec2Instance $shellCmd"
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
        sh "git remote set-url origin https://$USER:$PASSWORD@gitlab.com/ismailGitlab/ci-cd-pipeline-terraform-aws-jenkins.git"
    }

    sh "git add ."
    sh "git commit -m 'jenkins-ci: version bump'"
    sh "git push origin HEAD:main"
}

return this