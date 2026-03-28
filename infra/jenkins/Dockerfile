FROM jenkins/jenkins:lts-jdk17

USER root

# Install Maven
RUN apt-get update && apt-get install -y maven curl gnupg lsb-release apt-transport-https ca-certificates

# Install Docker CLI
RUN install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc && \
    chmod a+r /etc/apt/keyrings/docker.asc && \
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null && \
    apt-get update && apt-get install -y docker-ce-cli

# Install kubectl
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    chmod +x kubectl && mv kubectl /usr/local/bin/kubectl

# Install minikube
RUN curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64 && \
    chmod +x minikube-linux-amd64 && mv minikube-linux-amd64 /usr/local/bin/minikube

USER jenkins
