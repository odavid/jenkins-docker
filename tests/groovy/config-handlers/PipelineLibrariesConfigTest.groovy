import org.yaml.snakeyaml.Yaml

handler = 'PipelineLibraries'
configHandler = evaluate(new File("/usr/share/jenkins/config-handlers/${handler}Config.groovy"))

def testLibraries(){
 	def config = new Yaml().load("""
my-lib:
  defaultVersion: master
  implicit: true
  allowVersionOverride: false
  includeInChangesets: false
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    credentialsId: my-git-key
    includes: '*/master'
    excludes: '*/non-master'
    ignoreOnPushNotifications: true
my-lib-with-defaults:
  defaultVersion: master
  source:
    remote: git@github.com:odavid/my-bloody-jenkins.git
    credentialsId: my-git-key
dynamic-scm-source:
  defaultVersion: p4-version
  implicit: true
  retriever:
    scm:
      \$class: org.jenkinsci.plugins.p4.scm.GlobalLibraryScmSource
      path: //xxx/yyy
      credential: p4-creds
      charset:
""")

    configHandler.setup(config)
    def myLib = org.jenkinsci.plugins.workflow.libs.GlobalLibraries.get().libraries.find{ it.name == 'my-lib'}
    assert myLib.defaultVersion == 'master'
    assert myLib.implicit
    assert !myLib.allowVersionOverride
    assert !myLib.includeInChangesets
    assert myLib.retriever.scm.id == 'git-scm-my-lib'
    assert myLib.retriever.scm.remote == 'git@github.com:odavid/my-bloody-jenkins.git'
    assert myLib.retriever.scm.credentialsId == 'my-git-key'
    assert myLib.retriever.scm.includes == '*/master'
    assert myLib.retriever.scm.excludes == '*/non-master'
    assert myLib.retriever.scm.ignoreOnPushNotifications

    myLib = org.jenkinsci.plugins.workflow.libs.GlobalLibraries.get().libraries.find{ it.name == 'my-lib-with-defaults'}
    assert myLib.defaultVersion == 'master'
    assert !myLib.implicit
    assert myLib.allowVersionOverride
    assert myLib.includeInChangesets
    assert myLib.retriever.scm.id == 'git-scm-my-lib-with-defaults'
    assert myLib.retriever.scm.remote == 'git@github.com:odavid/my-bloody-jenkins.git'
    assert myLib.retriever.scm.credentialsId == 'my-git-key'
    assert myLib.retriever.scm.includes == '*'
    assert myLib.retriever.scm.excludes == ''
    assert !myLib.retriever.scm.ignoreOnPushNotifications

    myLib = org.jenkinsci.plugins.workflow.libs.GlobalLibraries.get().libraries.find{ it.name == 'dynamic-scm-source'}
    assert myLib.defaultVersion == 'p4-version'
    assert myLib.implicit
    assert (myLib.retriever.scm instanceof org.jenkinsci.plugins.p4.scm.GlobalLibraryScmSource)
    assert myLib.retriever.scm.credential == 'p4-creds'
    assert myLib.retriever.scm.path == '//xxx/yyy'
}

testLibraries()