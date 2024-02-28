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

        stage('deploy') {
            when {
                expression {
                    BRANCH_NAME == 'main'
                }
            }
            environment {
                APP_NAME = "java-maven-app"
                AWS_DEFAULT_REGION = 'us-east-2'
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