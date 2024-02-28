#!/bin/bash

# Update dnf package manager
sudo dnf update -y

# Install and start docker
sudo dnf install -y docker
sudo systemctl start docker

# Add user 'ec2-user' to docker group (to execute docker commands without sudo)
sudo usermod -aG docker ec2-user

# Install docker-compose
sudo curl -SL "https://github.com/docker/compose/releases/download/v2.24.6/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose