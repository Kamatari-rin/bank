pipeline {
  agent any

  options {
    ansiColor('xterm')
    timestamps()
  }

  parameters {
    choice(name: 'ENV', choices: ['dev', 'test', 'prod'], description: 'Целевое окружение')
    string(name: 'NAMESPACE', defaultValue: 'bank-dev', description: 'K8s namespace')
    booleanParam(name: 'DEPLOY', defaultValue: true, description: 'Деплоить в Kubernetes')
    booleanParam(name: 'RUN_HELM_TESTS', defaultValue: true, description: 'Запустить helm test после деплоя')
    string(name: 'IMAGE_TAG', defaultValue: '', description: 'Тег образов (по умолчанию короткий git SHA)')
    booleanParam(name: 'DEPLOY_KAFKA', defaultValue: false, description: 'Деплоить Kafka вместе с приложением')
  }

  environment {
    SERVICES = "accounts-service notifications-service blocker-service cash-service transfer-service exchange-service exchange-generator-service gateway"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Compute TAG') {
      steps {
        script {
          if (!params.IMAGE_TAG?.trim()) {
            env.IMAGE_TAG = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
          } else {
            env.IMAGE_TAG = params.IMAGE_TAG.trim()
          }
          echo "IMAGE_TAG = ${env.IMAGE_TAG}"
        }
      }
    }

    stage('Gradle build (jar + tests)') {
      steps {
        sh """
          ./gradlew --no-daemon clean build
        """
      }
      post {
        always {
          junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'
          archiveArtifacts allowEmptyArchive: true, artifacts: '**/build/libs/*.jar'
        }
      }
    }

    stage('Docker build images') {
      steps {
        script {
          def services = env.SERVICES.split()
          services.each { svc ->
            def img = svc.replace('-service','-service')
            sh """
              echo "Building image: ${img}:${IMAGE_TAG}"
              docker build -t ${img}:${IMAGE_TAG} ./${svc}
            """
          }
        }
      }
    }

    stage('Load images into minikube') {
      when { expression { return params.DEPLOY } }
      steps {
        script {
          def services = env.SERVICES.split()
          services.each { svc ->
            def img = svc.replace('-service','-service')
            sh """
              echo "minikube image load ${img}:${IMAGE_TAG}"
              minikube image load ${img}:${IMAGE_TAG}
            """
          }
        }
      }
    }

    stage('Helm lint') {
      steps {
        sh """
          helm lint helm
        """
      }
    }

    stage('Helm upgrade/install Kafka & Observability') {
      when { expression { return params.DEPLOY_KAFKA } }
      steps {
        sh """
          # Kafka (кластер + топики, в т.ч. bank-logs)
          helm upgrade --install bank-kafka ./helm/charts/kafka \\
            -n ${NAMESPACE} --create-namespace

          # Zipkin (распределённый трейсинг)
          helm upgrade --install zipkin ./helm/charts/zipkin \\
            -n ${NAMESPACE}

          # Prometheus (сбор метрик)
          helm upgrade --install prometheus ./helm/charts/prometheus \\
            -n ${NAMESPACE}

          # Grafana (дашборды)
          helm upgrade --install grafana ./helm/charts/grafana \\
            -n ${NAMESPACE}

          # Elasticsearch (хранилище логов)
          helm upgrade --install elasticsearch ./helm/charts/elasticsearch \\
            -n ${NAMESPACE}

          # Logstash (Kafka → ES)
          helm upgrade --install logstash ./helm/charts/logstash \\
            -n ${NAMESPACE}

          # Kibana (UI для логов)
          helm upgrade --install kibana ./helm/charts/kibana \\
            -n ${NAMESPACE}
        """
      }
    }


    stage('Helm upgrade/install') {
      when { expression { return params.DEPLOY } }
      steps {
        sh """
          helm upgrade --install bank ./helm \
            -n ${NAMESPACE} --create-namespace \
            -f helm/values-${ENV}.yaml \
            --set accounts.image.tag=${IMAGE_TAG} \
            --set cash.image.tag=${IMAGE_TAG} \
            --set transfer.image.tag=${IMAGE_TAG} \
            --set exchange.image.tag=${IMAGE_TAG} \
            --set exchangeGenerator.image.tag=${IMAGE_TAG} \
            --set blocker.image.tag=${IMAGE_TAG} \
            --set notifications.image.tag=${IMAGE_TAG} \
            --set gateway.image.tag=${IMAGE_TAG} \
            --wait --timeout 10m
        """
      }
    }

    stage('Helm tests') {
      when { allOf {
        expression { return params.DEPLOY }
        expression { return params.RUN_HELM_TESTS }
      } }
      steps {
        sh """
          helm test bank -n ${NAMESPACE} --logs
        """
      }
    }
  }

  post {
    always {
      echo "Pipeline finished. ENV=${params.ENV}, NAMESPACE=${params.NAMESPACE}, TAG=${env.IMAGE_TAG}"
    }
  }
}
