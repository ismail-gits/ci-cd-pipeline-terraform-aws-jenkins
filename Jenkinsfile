#!/usr/bin/env groovy

def gv

pipeline {
    agent any

    tools {
        maven 'maven-3.6'
    }

    stages {
        stage('init') {
            steps {
                script {
                    gv = load "script.groovy"
                    echo "Executing pipeline for branch $BRANCH_NAME"
                }
            }
        }

        stage('increment version') {
            steps {
                script {
                    gv.incrementVersion()
                }
            }
        }

        stage('test app') {
            steps {
                script {
                    gv.testApp()
                }
            }
        }

        stage('build app') {
            when {
                expression {
                    BRANCH_NAME == 'main'
                }
            }
            steps {
                script {
                    gv.buildApp()
                }
            }
        }

        stage('build image') {
            when {
                expression {
                    BRANCH_NAME == 'main'
                }
            }
            steps {
                script {
                    gv.buildImage()
                }
            }
        }

        stage('push image') {
            when {
                expression {
                    BRANCH_NAME == 'main'
                }
            }
            steps {
                script {
                    gv.pushImage()
                }
            }
        }

        stage('provision server') {
            when {
                expression {
                    BRANCH_NAME == 'main'
                }
            }
            environment {
                AWS_ACCESS_KEY_ID = credentials('aws_access_key_id')
                AWS_SECRET_ACCESS_KEY = credentials('aws_secret_access_key')
            }
            steps {
                script {
                    gv.provisionServer()
                }
            }
        }

        stage('deploy') {
            when {
                expression {
                    BRANCH_NAME == 'main'
                }
            }
            environment {
                DOCKER_CREDS = credentials('docker-credentials')
            }
            steps {
                script {
                    gv.deploy()
                }
            }
        }

        stage('version bump') {
            when {
                expression {
                    BRANCH_NAME == 'main'
                }
            }
            steps {
                script {
                    gv.versionBump()
                }
            }
        }
    }
}