# My Bloody Jenkins - An opinionated Jenkins Docker Image
[![Build Status](https://travis-ci.org/odavid/my-bloody-jenkins.svg?branch=master)](https://travis-ci.org/odavid/my-bloody-jenkins)
[![Docker Pulls](https://img.shields.io/docker/pulls/odavid/my-bloody-jenkins.svg)](https://hub.docker.com/r/odavid/my-bloody-jenkins/)

## Changelog

[See Changelog](CHANGELOG.md)

## What's in the Box?
*My Bloody Jenkins* is a re-distribution of the [Official LTS Jenkins Docker image](https://hub.docker.com/r/jenkins/jenkins/) bundled with most popular plugins and
ability to configure most aspects of Jenkins from a **simple** and **single source of truth** represented as YAML.

The image can get the configuration from several data sources such as: File, S3, Environment Variable, HTTP, Kubernetes ConfigMap and Kubernetes Secret.

The image supports "Watching" configuration changes and applying them immedately without restarting jenkins.

The image is "Battle Proven" and serves as the baseground for several Jenkins deployments in production.

## Features
* Configuration Coverage:
  * Security Realm (LDAP/AD/Simple Jenkins database)
  * Glabal Security Options
  * Authorization
  * Jenkins Clouds (Amazon ECS, Kubernetes, Docker)
  * Global Pipeline Libraries
  * Seed Jobs
  * JobDSL Scripts
  * Script approvals
  * Notifiers (Hipchat, Slack, Email, Email-Ext)
  * Credentials (aws, userpass, sshkeys, certs, kubernetes, gitlab, simple secrets)
  * Tools and installers (JDK, Ant, Maven, Gradle, SonarQube, Xvfb)
  * Misc. Plugins configuration such as Jira, SonarQube, Checkmarx
  * Misc. Configuration options such as Environment variables, Proxy
* Support additional plugins installation during startup without the need to build your own image
* Supports quiet startup period to enable docker restarts with a graceful time which Jenkins is in *Quiet Mode*
* Automated Re-Configure based on configuration data change without restarts
* Supports Dynamic Host IP configuration passed to clouds when Jenkins is running in a cluster
* Support dynamic envrionment variables from [consul](https://www.consul.io/) and [vault](https://www.vaultproject.io/) using [envconsul](https://github.com/hashicorp/envconsul)

## Why Use the term "Bloody"?
The term "My Bloody Jenkins" came from the fact that I tried to put all my "battle" experience, (i.e. blood, sweat and tears) within the image.
I just thought it is a "catchy" name for this kind of a repository.

## Demo
A [step by step demo](demo) can be found [here](demo)

## Some Usage Examples
* [docker-plugin cloud](examples/docker/) cloud using Docker Plugin cloud with seed job. See [examples/docker](examples/docker/)
* [kubernetes](examples/kubernetes/) cloud using Minikube with seed job. See [examples/kubernetes](examples/kubernetes/)

## Releases
Docker Images are pushed to [Docker Hub](https://hub.docker.com/r/odavid/my-bloody-jenkins/)

Each release is a git tag v$LTS_VERSION-$INCREMENT where:

* LTS_VERSION is the Jenkins LTS version
* INCREMENT is a number representing that representing the release contents (i.e additional configuration options, bugs in configuration, plugins, etc...)

For each git tag, there following tags will be created:
* $LTS_VERSION-$INCREMENT - one to one releationship with git tag
* $LTS_VERSION - latest release for that LTS version
* lts - represents the latest release

Each master commit, will be tagged as latest

```bash
# get the latest release
docker pull odavid/my-bloody-jenkins:lts
# get the latest 2.73.3 LTS
docker pull odavid/my-bloody-jenkins:2.73.3
# get a concrete 2.73.3 release
docker pull odavid/my-bloody-jenkins:2.73.3-6
# get the latest unstable image
docker pull odavid/my-bloody-jenkins
```


## Environment Variables
The following Environment variables are supported

* __JENKINS_ENV_ADMIN_USER__ - (***mandatory***) Represents the name of the admin user. If LDAP is your choice of authentication, then this should be a valid LDAP user id. If Using Jenkins Database, then you also need to pass the password of this user within the [configuration](#configuration-reference).

* __JAVA_OPTS\_*__ - All JAVA_OPTS_ variables will be appended to the JAVA_OPTS during startup. Use them to control options (system properties) or memory/gc options. I am using few of them by default to tweak some known issues:
    * JAVA_OPTS_DISABLE_WIZARD - disables the Jenkins 2 startup wizard
    * JAVA_OPTS_CSP - Default content security policy for HTML Publisher/Gatling plugins - See [Configuring Content Security Policy](https://wiki.jenkins.io/display/JENKINS/Configuring+Content+Security+Policy)
    * JAVA_OPTS_LOAD_STATS_CLOCK - This one is sweet (: - Reducing the load stats clock enables ephemeral slaves to start immediately without waiting for suspended slaves to be reaped

* __JENKINS_ENV_CONFIG_YAML__ - The [configuration](#configuration-reference) as yaml. When this variable is set, the contents of this variable can be fetched from Consul and also be watched so jenkins can update its configuration everytime this variable is being changed. Since the contents of this variable contains secrets, it is wise to store and pass it from Consul/S3 bucket. In any case, before Jenkins starts, this variable is being unset, so it won't appear in Jenkins 'System Information' page (As I said, blood...)

* __JENKINS_ENV_CONFIG_YML_URL__ - A comma separated URLs that will be used to fetch the configuration and updated jenkins everytime the change. This is an alternative to __JENKINS_ENV_CONFIG_YAML__ setup.
Supported URLs:
  * `s3://<s3path>` - s3 path
  * `file://<filepath>` - a file path (should be mapped as volume) - can be a file, folder or glob expression (e.g. `file:///dir/filename` or `file:///dir` or `file:///dir/*.yml`)
  * `http[s]://<path>` - an http endpoint

> Note: If multiple URLs are passed or the file url contains a dir name or a glob expression, all yaml files are being deep merged top to bottom. This behavior enables to separate the configuration into different files or override default configuration.


* __JENKINS_ENV_CONFIG_YML_URL_DISABLE_WATCH__ - If equals to 'true', then the configuration file will be fetched only at startup, but won't be watched. Default 'false'

* __JENKINS_ENV_CONFIG_YML_URL_POLLING__ - polling interval in seconds to check if file changed in s3. Default (30)

* __JENKINS_ENV_HOST_IP__ - When Jenkins is running behind an ELB or a reverse proxy, JNLP slaves must know about the real IP of Jenkins, so they can access the 50000 port. Usually they are using the Jenkins URL to try to get to it, so it is very important to let them know what is the original Jenkins IP Address. If the master has a static IP address, then this variable should be set with the static IP address of the host.

* __JENKINS_ENV_HOST_IP_CMD__ - Same as ___JENKINS_ENV_HOST_IP___, but this time a shell command expression to fetch the IP Address. In AWS, it is useful to use the EC2 Magic IP: ```JENKINS_ENV_HOST_IP_CMD='curl http://169.254.169.254/latest/meta-data/local-ipv4'```

* __JENKINS_HTTP_PORT_FOR_SLAVES__ - (Default: 8080) Used together with JENKINS_ENV_HOST_IP to construct the real jenkinsUrl for jnlp slaves.

* __JENKINS_ENV_JENKINS_URL__ - Define the Jenkins root URL in configuration. This can be useful when you cannot run the Jenkins master docker container with host network and you need it to be available to slaves

* __JENKINS_ENV_ADMIN_ADDRESS__ - Define the Jenkins admin email address

* __JENKINS_ENV_PLUGINS__ - Ability to define comma separated list of additional plugins to install before starting up. See [plugin-version-format](https://github.com/jenkinsci/docker#plugin-version-format). This is option is not recommended, but sometimes it is useful to run the container without creating an inherited image.

* __JENKINS_ENV_QUIET_STARTUP_PERIOD__ - Time in seconds. If speficied, jenkins will start in quiet mode and disable all running jobs. Useful for major upgrade.

## Configuration Reference
The configuration is divided into main configuration sections. Each section is responsible for a specific aspect of jenkins configuration.

### Environment Variables Section
Responsible for adding global environment variables to jenkins config.
Keys are environment variable names and values are their corresponding values. Note that variables names should be a valid environment variable name.

```yaml
  environment:
    ENV_KEY_NAME1: ENV_VALUE1
    ENV_KEY_NAME2: ENV_VALUE1

```

### Environment variable Substitution and Remove Master Env Vars
You can use ```${ENV_VAR_NAME}``` within the config.yml in order to use environment variables substitution for sensitive data (e.g k8s secrets).
When you pass secrets environment variables to the container, Jenkins will display them in the 'System Info' page. In order to disable that beheviour, you can use
```remove_master_envvars``` section and add regular expressions for variables you don't want to show on the SystemInfo page.

> Escaping `${VAR}` to be used as is without substitution, is done by using `\${VAR}` within the yaml file

```yaml
security:
  realm: ldap
  managerDN: cn=search-user,ou=users,dc=mydomain,dc=com
  managerPassword: '${LDAP_PASSWORD}' # Use LDAP_PASSWORD environment variable
  ...

remove_master_envvars:
  - '.*PASS.*'
  - '.*SECRET.*'
  - 'MY_SPECIAL_VARIABLE'
```

### Environment Variables Data Sources
The image supports the following data sources for environment variables:
* Native - Environment variables that are passed to the container at startup
* [Files](#environment-variables-values-from-files) - By passing `ENVVARS_DIRS` variable to the container, selected directories can be treated as environment variable source
* [Consul](#using-envconsul-to-fetch-dynamic-environment-variables-from-consul-and-vault) - using [envconsul](https://github.com/hashicorp/envconsul)
* [Vault](#using-envconsul-to-fetch-dynamic-environment-variables-from-consul-and-vault) - using [envconsul](https://github.com/hashicorp/envconsul)


### Environment Variables Values From Files
When using [Environment Variable Substitution](#environment-variable-substitution) within the config.yml file, you can consume environment variables values directly from files contents within folders. This is useful especially when using [k8s secrets volume mappings](https://kubernetes.io/docs/concepts/storage/volumes/#secret)

In order to activate this feature, you need to pass `ENVVARS_DIRS` variable to the container with a comma separated list of directories.

__Example__

Assuming you have the following files within the container:
* /var/secret/username
* /var/secret/password
* /var/other-secret/ssh-key
* /var/other-secret/api-token

Setting the following `ENVVARS_DIRS` environment variable as follows:
```shell
ENVVARS_DIRS=/var/secret/,/var/other-secret
```
Will produce the following environment variables:
* `SECRET_USERNAME` - contents of `/var/secret/username`
* `SECRET_PASSWORD` - contents of `/var/secret/password`
* `OTHER_SECRET` - contents of `/var/other-secret/ssh-key`
* `OTHER_API_TOKEN` - contents of `/var/other-secret/api-token`

> Note that variable names are the `<FOLDER_NAME>_<FILE_NAME>` sanitized and uppercased

### Using envconsul to Fetch Dynamic Environment Variables from Consul and Vault
When using [Environment Variable Substitution](#environment-variable-substitution) within the config.yml file, you can direct the container to automatically fetch them from from [consul](https://www.consul.io/) and [vault](https://www.vaultproject.io/) using [envconsul](https://github.com/hashicorp/envconsul)

The following environment variables need to be provided in order to support it:

* `ENVCONSUL_CONSUL_PREFIX` - Comma separated values of consul key prefixes - Mandatory if using consul to fetch information
* `CONSUL_ADDR` - Consul address (host:port) - Mandatory if using consul to fetch information
* `CONSUL_TOKEN` - Consul ACL Token - The token that used to be authorize the container to fetch the keys from consul - Mandatory if consul ACLs are in use
* `ENVCONSUL_VAULT_PREFIX` - Comma separated values of vault key prefixes - Mandatory if using vault to fetch information
* `VAULT_ADDR` - Vault address \(http\[s]://host:port) - Mandatory if using vault to fetch information
* `VAULT_TOKEN` - Vault ACL Token - The token that used to be authorize the container to fetch the keys from vault - Mandatory
* `ENVCONSUL_UNWRAP_TOKEN` - true/false (default = false), see - tells Envconsul that the provided token is actually a wrapped token that should be unwrapped using Vault's [cubbyhole response wrapping](https://www.vaultproject.io/guides/secret-mgmt/cubbyhole.html)
* `ENVCONSUL_MAX_RETRIES` - (default = 5), How many time the envconsul will retry to fetch data
* `ENVCONSUL_ADDITIONAL_ARGS` - A list of command line arguments to append to the [envconsul](https://github.com/hashicorp/envconsul) CLI. For more details, please read the [envconsul READM](https://github.com/hashicorp/envconsul/blob/master/README.md)

The following parameters are being added to the envconsul CLI:
* -sanitize - replaces all invalid characters to underscore
* -upcase - All keys will become Uppercase

> Due to [An open Issue with envconsul and vault > 0.9.6](https://github.com/hashicorp/envconsul/issues/175), Only Vault versions <= 0.9.6 can be used


### Security Section
Responsible for:
* Setting up security realm
    * jenkins_database - the adminPassword must be provided
    * ldap - LDAP Configuration must be provided
    * active_directory - Uses [active-directory plugin](https://wiki.jenkins.io/display/JENKINS/Active+Directory+plugin)
* User/Group Permissions dict - Each key represent a user or a group and its value is a list of Jenkins [Permissions IDs](https://wiki.jenkins.io/display/JENKINS/Matrix-based+security)

```yaml
# jenkins_database - adminPassword must be provided
security:
    realm: jenkins_database
    adminPassword: S3cr3t
```

```yaml
# ldap - ldap configuration must be provided
security:
    realm: ldap
    server: myldap.server.com:389 # mandatory
    rootDN: dc=mydomain,dc=com # mandatory
    managerDN: cn=search-user,ou=users,dc=mydomain,dc=com # mandatory
    managerPassword: <passowrd> # mandatory
    userSearchBase: ou=users
    userSearchFilter: uid={0}
    groupSearchBase: ou=groups
    groupSearchFilter: cn={0}
    ########################
    # Only one is mandatory - depends on the group membership strategy
    # If a user record contains the groups, then we need to set the
    # groupMembershipAttribute.
    # If a group contains the users belong to it, then groupMembershipFilter
    # should be set.
    groupMembershipAttribute: group
    groupMembershipFilter: memberUid={1}
    ########################
    disableMailAddressResolver: false # default = false
    connectTimeout: 5000 # default = 5000
    readTimeout: 60000 # default = 60000
    displayNameAttr: cn
    emailAttr: email
```

```yaml
# active_directory - active_directory configuration must be provided
security:
    realm: active_directory
    domains:
      - name: corp.mydomain.com
        servers:
          - dc1.corp.mydomain.com
          - dc2.corp.mydomain.com
        site: optional-site
        bindName: CN=user,OU=myorg,OU=User,DC=mydoain,DC=com
        bindPassword: secret
    groupLookupStrategy: AUTO # AUTO, RECURSIVE, CHAIN, TOKENGROUPS
    removeIrrelevantGroups: false
    cache:
      size: 500
      ttl: 30
    startTls: false
    tlsConfiguration: TRUST_ALL_CERTIFICATES # TRUST_ALL_CERTIFICATES, JDK_TRUSTSTORE
    jenkinsInternalUser: my-none-ad-user #
```

```yaml
# Permissions - each key represents a user/group and has list of Jenkins Permissions
security:
  realm: ...
  permissions:
    authenticated: # Special group
      - hudson.model.Hudson.Read # Permission Id - see
      - hudson.model.Item.Read
      - hudson.model.Item.Discover
      - hudson.model.Item.Cancel
    junior-developers:
      - hudson.model.Item.Build
```

```yaml
# Misc security options
security:
  securityOptions:
    preventCSRF: true # default true
    enableScriptSecurityForDSL: false # default false
    enableCLIOverRemoting: false # default false
    enableAgentMasterAccessControl: true # default true
    disableRememberMe: false # default false
    sshdEnabled: true # default false, if true, port 16022 is exposed
    jnlpProtocols: # by default only JNLP4 is enabled
      - JNLP
      - JNLP2
      - JNLP3
      - JNLP4
```

### Tools Section
Responsible for:
* Setting up tools locations and auto installers

The following tools are currently supported:
* JDK - (type: jdk)
* Apache Ant (type: ant)
* Apache Maven (type: maven)
* Gradle (type: gradle)
* Xvfb (type: xvfb)
* SonarQube Runner (type: sonarQubeRunner)

The following auto installers are currently supported:
* Oracle JDK installers
* Maven/Gradle/Ant/SonarQube version installers
* Shell command installers (type: command)
* Remote Zip/Tar files installers (type: zip)

The tools section is a dict of tools. Each key represents a tool location/installer ID.
Each tools should have either home property or a list of installers property.

Note: For Oracle JDK Downloaders to work, the oracle download user/password should be provided as well.

```yaml
tools:
  oracle_jdk_download: # The oracle download user/password should be provided
    username: xxx
    password: yyy
  installations:
    JDK8-u144: # The Tool ID
      type: jdk
      installers:
        - id: jdk-8u144-oth-JPR # The exact oracle version id
    JDK7-Latest:
      type: jdk
      home: /usr/java/jdk7/latest # The location of the jdk
    MAVEN-3.1.1:
      type: maven
      home: /usr/share/apache-maven-3.1.1
    MAVEN-3.5.0:
      installers:
        - id: '3.5.0' # The exact maven version to be downloaded
    ANT-1.9.4:
      type: ant
      home: /usr/share/apache-ant-1.9.4
    ANT-1.10.1:
      type: ant
      installers:
        - id: '1.10.1' # The exact ant version to be downloaded
    GRADLE-4.2.1:
      type: gradle
      ... # Same as the above - home/installers with id: <gradle version>
    SONAR-Default:
      type: sonarQubeRunner
      installers:
        - id: '3.0.3.778'
      ... # Same as the above - home/installers with id: <sonar runner version>

    # zip installer and shell command installers
    ANT-XYZ:
      type: ant
      installers:
        - type: zip
          label: centos7 # nodes labels that will use this installer
          url: http://mycompany.domain/ant/xyz/ant.tar.gz
          subdir: apache-ant-zyz # the sub directoy where the tool exists within the zip file
        - type: command
          label: centos6 # nodes labels that will use this installer
          command: /opt/install-ant-xyz
          toolHome: # the directoy on the node where the tool exists after running the command
```

### Credentials Section
Responsible for:
* Setting up [credentials](https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin) to be used later on by pipelines/tools
* Each credential has an id, type, description and arbitary attributes according to its type
* The following types are supported:
    * type: text - [simple secret](https://wiki.jenkins.io/display/JENKINS/Plain+Credentials+Plugin). Mandatory attributes:
        * text - the text to encrypt
    * type: aws - an [aws secret](https://wiki.jenkins.io/display/JENKINS/CloudBees+AWS+Credentials+Plugin). Mandatory attributes:
        * access_key - AWS access key
        * secret_access_key - AWS secret access key
    * type: userpass - a [user/password](https://github.com/jenkinsci/credentials-plugin/blob/master/src/main/java/com/cloudbees/plugins/credentials/impl/UsernamePasswordCredentialsImpl.java) pair. Mandatory attributes:
        * username
        * password
    * type: sshkey - an [ssh private key](https://wiki.jenkins.io/display/JENKINS/SSH+Credentials+Plugin) in PEM format. Mandatory attributes:
        * username
        * privatekey - PEM format text
        * base64 - PEM format text base64 encoded. Mandatory if privatekey is not provided
        * fileOnMaster - A path that is accessible within the docker container. This option is required if privateKey or base64 are not provided. Also, this option is optimal when using _Kubernetes Secrets_
        * passphrase - not mandatory, but encouraged

    * type: cert - a [Certificate](https://wiki.jenkins.io/display/JENKINS/Credentials+Plugin). Mandatory attributes:
      * base64 - the PKCS12 certificate bytes base64 encoded
      * fileOnMaster - A path that is accessible within the docker container. This option is required if base64 is not provided. Also, this option is optimal when using _Kubernetes Secrets_
      * password - not mandatory, but encouraged
    * type: gitlab-api-token - a [Gitlab API token](https://wiki.jenkins.io/display/JENKINS/GitLab+Plugin) to be used with the gitlab plugin
      * text - the api token as text

> `Note: Currently the configuration supports only the global credentials domain.`

#### Dynamic Credentials

When the type attribute is not one of the above types, the configuration will try to find the right credential type and configure it using [org.jenkinsci.plugins.structs.describable.DescribableModel](https://github.com/jenkinsci/structs-plugin/blob/master/plugin/src/main/java/org/jenkinsci/plugins/structs/describable/DescribableModel.java)

The logic for dealing with unknown types is as follows:
* If the type: ```fully.qualified.credentials.class.name```, then we will try to use that class name
  * ```type: org.jenkinsci.plugins.p4.credentials.P4TicketImpl``` will try to instantiate a [org.jenkinsci.plugins.p4.credentials.P4TicketImpl](https://github.com/jenkinsci/p4-plugin/blob/master/src/main/java/org/jenkinsci/plugins/p4/credentials/P4TicketImpl.java) credentials
* If the type: ```simpleNameWithoutDots```, then we will search all the available __Credentials Descriptors__ and we will try to find the one that starts with the same name ignoring case
  * ```type: usernamePassword``` will match [com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl](https://github.com/jenkinsci/credentials-plugin/blob/master/src/main/java/com/cloudbees/plugins/credentials/impl/UsernamePasswordCredentialsImpl.java)
  * ```type: usernamePasswordImpl``` will match the same above
  * ```type: usernamepassword``` will match the same above

> `Note: When dealing with dynamic credentials, you will have to follow the @DataBoundConstructor and @DataBoundSetter rules`


```yaml
# Each top level key represents the credential id
credentials:
  slack:
    type: text
    description: The slace secret token
    text: slack-secret-token
  hipchat:
    type: text
    text: hipchat-token
  awscred:
    type: aws
    access_key: xxxx
    secret_access_key: yyyy
  gituserpass:
    type: userpass
    username: user
    password: password1234
  my-gitlab-api-token:
    type: gitlab-api-token
    text: <api-token-of-gitlab>
  gitsshkey:
    type: sshkey
    description: git-ssh-key
    username: user
    passphrase: password1234
    privatekey: | ## This is why I love yaml... So simple to handle text with newlines
      -----BEGIN RSA PRIVATE KEY-----
      Proc-Type: 4,ENCRYPTED
      DEK-Info: AES-128-CBC,B1615A0CA4F11333D058A5A9C4D0144E

      wxr6qxA/gcj+Bf1he0YRaqH2HbvHXIshrXTFq7oet5OeF1oaX4yyejotI9oPXvvM
      X9jzLpPwhdpriuFKJKr9jc+1rto/71QExTYEaAWwfYi1EVb1ERGmG4DMANqBKssO
      FTn01t4wew3d6DPcAIwFBT7gvlJfg0poCxae1fhsXGijMy2YryiU+BcV0BYsM6Lj
      VAfn9+djoxPKTv3wPFZPXVrzSkWG8IFUcLBIKE6hf3xxwV5FPHDSegAnwTBV9sVB
      BkVfAHDkzecEtK3iHqa9QUsW014TTLZ7Rbbzh6mskrGxgjgDXXjbdEYbJDtSES6E
      d2o1nXqJEsDdUrSWAoaViR052KyW8f8n//LEjI1a6aveOQWoWXgPwD9jnp5cPrGv
      mWGrZmhWLh0rx61qG+bBVEfbGmLmbi74jxq1/6vaAF4ChtfBks2mpOMMOKf+bKar
      w1laJKgwFRWO7iYql9dHzny2GXy4z6hjD5g3omEdFyWh3GSW8NNkXyojml7tlIa6
      ln286//PSmN0dLZptULAr8A4Bp+aGgybnla7H2F5/s2mGc39MrodFPFbpjp50d4R
      2x4uV4ofVvVExv5wWSdQ60o7trSvBWqwu4MDm2yWCxUiay8I8EF2iM0etutlWm5N
      V9aD8TTC5zHLbwY7YI0OvOXyCWgI5CW1tTsnoDxR1H0aDByuSL+Q+8I5gxRKdJlb
      fYZ683g8AuTkKilHQ6KINAAUuvMvgSWzOa/BU9L7Xqn99w2WgdheLMkdp6O4dJX4
      f4vFTzms+tBVXwqybac/8HZ4uW163pgnBpH2bVFi7/qyd8sl8TYAODN70R2oLv3c
      HBjzI/078G9B4WkpL99FK9cWsfCleIM+HIGeQ9jPDK/hzAhlKIIQxzIaomhcRZlW
      Oc3+zlioBuQSBVtf1UJMrCFSfr9Gq+lbhsD5k8bscU38d5R8EHDGvicpr/KIwjGn
      piNmO7Nz2KcJaLUX6D1oG0p4ioan7js27eBGVnur73hNySbFycwiTGdZkp1CHM7D
      OH2iOMibQGMGYWoci/SoIOgp54Meq4WkEpV5wwr6aCuTzsgnVJXVuMmBk8dnvyat
      EKflp+2kKCj9uPo/QUoozNAuNzyDJU95E8ZfLyT5G9GCQtlf2EnrFZIeQ040xBQC
      6vGQLodjxIG1X8ejDv69FIW66qyofAWVuwCO6wuCWEdLLZrNNhjyPCnnxv5Kw8oC
      nB8/YDntqKg2GqpDu4s0bzAt0CPbMQydvD5x4AWKYCm4HQIF3qX544yUKd9vuO3l
      5t8JE8l4ETGMieKFpE9YiVbobye8iNRyIYVBuvlk4lq+xifw7i3Crmr/+KB+2ABZ
      8TshgEjw8G7TR7qZjZGONCBJ6ozUNR5ipUANc81AA3AUGilBeC4lLUcvatsixtWz
      6BDHwOYfLbfm8YIxDELMt13f++sxKoed4EjFJu+JIjEZlRomPf9pZEmZwTRVASsc
      CbwJjRc022b7HIsetGBYu76KK/Fs25D5JTZjQ3ylMKwhBjrOT7d8Xm90/6eg4hvE
      4c9bdLH+2Xuc6qv/oBoFzVd19c3DiVfns2/5BohfG+pbNwZUVR1vjP/BVDgwDBc+
      -----END RSA PRIVATE KEY-----

  kubernetes-cert:
    type: cert
    password: secret
    base64: >
      MIIM2QIBAzCCDJ8GCSqGSIb3DQEHAaCCDJAEggyMMIIMiDCCBz8GCSqGSIb3DQEHBq
      CCBzAwggcsAgEAMIIHJQYJKoZIhvcNAQcBMBwGCiqGSIb3DQEMAQYwDgQIPPHR3lAy
      ...
```

### Notifiers Section
Responsible for Configuration of the following notifiers:
* Mail - [Default Mailer](https://wiki.jenkins.io/display/JENKINS/Mailer) and [Email-ext](https://wiki.jenkins.io/display/JENKINS/Email-ext+plugin) plugin
* [Slack plugin](https://wiki.jenkins.io/display/JENKINS/Slack+Plugin)
* [Hipchat plugin](https://wiki.jenkins.io/display/JENKINS/Hipchat+Plugin)

```yaml
notifiers:
  mail:
    host: xxx.mydomain.com
    port: 25
    authUser: test
    autPassword: test
    replyToAddress: no-reply@mydomain.com
    defaultSuffix: '@mydomain.com'
    useSsl: true
    charset: UTF-8
  hipchat:
    server: my.api.hipchat.com
    room: JenkinsNotificationRoom
    sendAs: TestSendAs
    v2Enabled: true
    credentialId: hipchat # should be defined in credentials section
  slack:
    teamDomain: teamDoamin
    botUser: true
    room: TestRoom
    baseUrl: http://localhost:8080/
    sendAs: JenkinsSlack
    credentialId: slack # should be defined in credentials section
```

### Pipeline Libraries Section
Responsible for setting up [Global Pipeline Libraries](https://wiki.jenkins.io/display/JENKINS/Pipeline+Shared+Groovy+Libraries+Plugin)

Configuration is composed of dicts where each top level key is the name of the library and its value contains the library configuration (source, default version)
```yaml
pipeline_libraries:
  my-library: # the library name
    source:
      remote: git@github.com:odavid/jenkins-docker.git
      credentialsId: gitsshkey # should be defined in credentials section
    defaultVersion: master
    implicit: false # Default false - if true the library will be available within all pipeline jobs with declaring it with @Library
    allowVersionOverride: true # Default true, better to leave it as is
    includeInChangesets: true # see https://issues.jenkins-ci.org/browse/JENKINS-41497

  my-other-lib:
    source:
      remote:
    ...

```

### Script Approval Section
Responsible for configuration list of white-listed methods to be used within your pipeline groovy scripts. See [Script Security](https://wiki.jenkins.io/display/JENKINS/Script+Security+Plugin)

Contains list of method/field signatures that will be added to the [Script Approval Console](https://wiki.jenkins.io/display/JENKINS/Script+Security+Plugin)

```yaml
script_approval:
  approvals:
    - field hudson.model.Queue$Item task
    - method groovy.lang.Binding getVariable java.lang.String
    - method groovy.lang.Binding getVariables
    - method groovy.lang.Binding hasVariable java.lang.String
    - method hudson.model.AbstractCIBase getQueue
    - staticMethod java.lang.Math max int int
    - staticMethod java.lang.Math max long long
    - staticMethod java.lang.Math min int int
    - staticMethod java.lang.Math min long long
    - staticMethod java.lang.Math pow double double
    - staticMethod java.lang.Math sqrt double
```

### Clouds Section
Responsible for configuration of the following docker cloud providers:
* Docker - [type: docker](https://wiki.jenkins.io/display/JENKINS/Docker+Plugin)
* Amazon ECS - [type: ecs](https://wiki.jenkins.io/display/JENKINS/Amazon+EC2+Container+Service+Plugin)
* Kubernetes - [type: kubernetes](https://wiki.jenkins.io/display/JENKINS/Kubernetes+Plugin)

You can define multiple clouds. Each cloud is configured as a dict. For top level key represents the cloud name. Each dict has a mandatory type attritube, and a section of templates representing slave docker templates that correspond to list of labels.

#### Amazon ECS
```yaml
clouds:
  # Top level key -> name of the cloud
  ecs-cloud:
    # type is mandatory
    type: ecs
    # If your jenkins master is running on EC2 and is using IAM Role, then you can
    # discard this credential, otherwise, you need to have an
    # aws credential declared in the credentials secion
    credentialsId: 'my-aws-key'
    # AWS region where your ECS Cluster reside
    region: eu-west-1
    # ARN of the ECS Cluster
    cluster: 'arn:ssss'
    # Timeout (in second) for ECS task to be created, usefull if you use large docker
    # slave image, because the host will take more time to pull the docker image
    # If empty or <= 0, the 900 is the default.
    connectTimeout: 0
    # List of templates
    templates:
      - name: ecsSlave
        # Only JNLP slaves are supported
        image: jenkinsci/jnlp-slave:latest
        # Labels are mandatory!
        # Your pipeline jobs will need to use node(label){} in order to use
        # this slave template
        labels:
          - ecs-slave
        # The directory within the container that is used as root filesystem
        remoteFs: /home/jenkins
        # JVM arguments to pass to the jnlp jar
        jvmArgs: -Xmx1g
        # ECS memory reservation
        memoryReservation: 2048
        # ECS cpu reservation
        cpu: 1024
        # Volume mappings
        # If your slave need to build docker images, then map the host docker socket
        # to the container docker socket. Also make sure the user within the container
        # has privileges to that socket within the entrypoint
        volumes:
          - '/var/run/docker.sock:/var/run/docker.sock'
        # Environment variables to pass to the slave container
        environment:
          XXX: xxx

        # an iam role arn for the task. If omitted, the EC2 instance IAM Role that runs the task will be in use
        taskrole: '<TASKROLE_ARN>'

        #########################
        ## FARGATE Only
        #########################
        launchType: FARGATE
        securityGroups: sg-123,sg-456         # comma separated security groups
        assignPublicIp: false                 # default false
        subnets: subnet-123,subnet-456        # comma separated subnet ids
        executionRole: ecsTaskExecutionRole   # by default 'ecsTaskExecutionRole' - see https://github.com/jenkinsci/amazon-ecs-plugin/pull/62
```


#### Kubernetes
```yaml
clouds:
  # Top level key -> name of the cloud
  kube-cloud:
    # type is mandatory
    type: kubernetes
    # Kubernetes URL
    serverUrl: http://mykubernetes
    # Default kubernetes namespace for slaves
    namespace: jenkins
    # Pod templates
    templates:
      - name: kubeslave
        # Only JNLP slaves are supported
        image: jenkinsci/jnlp-slave:latest
        # Labels are mandatory!
        # Your pipeline jobs will need to use node(label){} in order to use this slave template
        labels:
          - kubeslave
        # The directory within the container that is used as root filesystem
        remoteFs: /home/jenkins
        # JVM arguments to pass to the jnlp jar
        jvmArgs: -Xmx1g
        # Volume mappings
        # If your slave need to build docker images, then map the host docker socket
        # to the container docker socket. Also make sure the user within the container
        # has privileges to that socket within the entrypoint
        volumes:
          - '/var/run/docker.sock:/var/run/docker.sock'
        # Environment variables to pass to the slave container
        environment:
          XXX: xxx
```

#### Docker Cloud
```yaml
clouds:
  # Top level key -> name of the cloud
  docker-cloud:
    # type is mandatory
    type: docker
    templates:
      - name: dockerslave
        ## How many containers can run at the same time
        instanceCap: 100
        # Only JNLP slaves are supported
        image: jenkinsci/jnlp-slave:latest
        # Labels are mandatory!
        # Your pipeline jobs will need to use node(label){} in order to use this slave template
        labels:
          - dockerslave
        # The directory within the container that is used as root filesystem
        remoteFs: /home/jenkins
        # JVM arguments to pass to the jnlp jar
        jvmArgs: -Xmx1g
        # Volume mappings
        # If your slave need to build docker images, then map the host docker socket
        # to the container docker socket. Also make sure the user within the container
        # has privileges to that socket within the entrypoint
        volumes:
          - '/var/run/docker.sock:/var/run/docker.sock'
        # Environment variables to pass to the slave container
        environment:
          XXX: xxx
```


### Seed Jobs Section
Responsible for seed job creation and execution. Each seed job would be a pipeline script that can use [jobDsl pipeline step](https://github.com/jenkinsci/job-dsl-plugin/wiki/User-Power-Moves#use-job-dsl-in-pipeline-scripts)

```yaml
seed_jobs:
  # Each top level key is the seed job name
  SeedJob:
    source:
      # git repo where of the seed job
      remote: git@github.com:odavid/my-bloody-jenkins.git
      credentialsId: gitsshkey
      branch: 'master'
    triggers:
      # scm polling trigger
      pollScm: '* * * * *'
      # period trigger
      periodic: '* * * * *'
    # Location of the pipeline script within the repository
    pipeline: example/SeedJobPipeline.groovy
    # always - will be executed everytime the config loader will run
    # firstTimeOnly - will be executed only if the job was not exist
    # never - don't execute the job, let the triggers do their job
    executeWhen: always #firstTimeOnly always never

    # Define parameters with default values to the seed job
    parameters:
      a_boolean_param: # the name of the param
        type: boolean
        value: true
      a_string_param:
        type: string
        value: The String default value
      a_password_param:
        type: password
        value: ThePasswordValue
      a_choice_param:
        type: choice
        choices:
          - choice1 ## The first choice is the default one
          - choice2
          - choice3
      a_text_param:
        type: text
        value: |
          A text with
          new lines

```
### JobDSL Scripts Section
A "lighter" version of [Seed Jobs Section](#seed-jobs-section). Contains a list of [jobdsl scriptlets](https://github.com/jenkinsci/job-dsl-plugin/wiki). Each script will be executed on startup without creating a dedicated job.

```yaml
# Each list item will be running at startup and during update
job_dsl_scripts:
  - |
    // Creates a folder
    folder('foo')
  - |
    // Creates a free style project
    job('foo/bar'){
      scm {
          git('git://github.com/foo/bar.git')
      }
      triggers {
          scm('H/15 * * * *')
      }
      steps {
          maven('-e clean test')
      }
    }
```

### Running Jenkins behind proxy
When running Jenkins behind a proxy server, add the following to your config yaml file:

```yaml

proxy:
  proxyHost: <proxy_address>
  port: <port>
  username: <proxy_auth> # leave empty if not required
  password: <proxy_auth> # leave empty if not required
  noProxyHost: <comma delimited>

```

Also, you should pass the following environment variables to the container:
* http_proxy
* https_proxy
* no_proxy


### Custom Config Handler
Sometimes, there is a need to have additional configuration that is not supported by this image.
For that purpose, we have a __custom configuration hook__

In order to use this hook, you will need to create your own docker image and add the following:

* Have ```/usr/share/jenkins/config-handlers/CustomConfig.groovy``` within your image with a single method as an entrypoint:

```groovy
def setup(config){

}
return this
```

* In the configuration yaml file, have a ```customConfig``` top level key that will be passed to the above groovy script.

