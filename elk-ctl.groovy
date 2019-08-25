node ("masterLin") {

    //Первый шаг, загрузка проекта из GIT
    stage("Загрузка проекта") {
        git_project='ssh://git@sbrf-bitbucket.ca.sbrf.ru:7999/dev/devops-deploy.git'
        git_branch = 'master'//"$BRANCH"
        cred_id = "devops-deploy"
        echo "Старт загрузки проекта из GIT GIT"
        checkout changelog: false, poll: false, scm: [$class: 'GitSCM', branches: [[name: "*/${git_branch}"]], doGenerateSubmoduleConfigurations: false,
        extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: "${env.WORKSPACE}"], [$class: 'SubmoduleOption', disableSubmodules: false,
        parentCredentials: true, recursiveSubmodules: true, reference: '', timeout: 1, trackingSubmodules: false]], submoduleCfg: [],
        userRemoteConfigs: [[credentialsId: cred_id, url: git_project]]]
    }

    //Определяем список серверов для ansible.
    // if (CUSTOM_HOSTS != "") {
    //     HOSTS = CUSTOM_HOSTS
    // }

    segment='alpha'
    ENVIRONMENT='prom'
    TASK='test'
    HOSTS='alpha_all'
    //Основной этап $TASK
    stage("Запуск $TASK") {
        withCredentials([file(credentialsId: 'devops-deploy-vault', variable: 'VAULT_PASSWORD_FILE')]) {
            ansiblePlaybook playbook: "ansible/elk-ctl.yml", inventory: "ansible/environment/$segment/$ENVIRONMENT/all.ini",  extras: "--vault-password-file=\"$VAULT_PASSWORD_FILE\" -e \"hosts=$HOSTS task=$TASK\""
        }
    }
}