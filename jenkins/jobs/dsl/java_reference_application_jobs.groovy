
import pluggable.scm.*;

SCMProvider scmProvider = SCMProviderHandler.getScmProvider("${SCM_PROVIDER_ID}", binding.variables)

// Folders
def workspaceFolderName = "${WORKSPACE_NAME}"
def projectFolderName = "${PROJECT_NAME}"
def projectScmNamespace = "${SCM_NAMESPACE}"

// Variables
def projectNameKey = projectFolderName.toLowerCase().replace("/", "-")
def referenceAppgitRepo = "spring-petclinic"
def regressionTestGitRepo = "adop-cartridge-java-regression-tests"
def scmUrl = scmProvider.getScmUrl()
def referenceAppGitUrl = scmUrl + "${SCM_NAMESPACE}/" + referenceAppgitRepo
def regressionTestGitUrl = scmUrl + "${SCM_NAMESPACE}/" + regressionTestGitRepo

// ** The logrotator variables should be changed to meet your build archive requirements
def logRotatorDaysToKeep = 7
def logRotatorBuildNumToKeep = 7
def logRotatorArtifactsNumDaysToKeep = 7
def logRotatorArtifactsNumToKeep = 7

// Jobs
def pipelineAppJob = pipelineJob(projectFolderName + "/Java_Reference_Application")

pipelineAppJob.with {
  description("Reference Application Pipeline.")
  logRotator {
    daysToKeep(logRotatorDaysToKeep)
    numToKeep(logRotatorBuildNumToKeep)
    artifactDaysToKeep(logRotatorArtifactsNumDaysToKeep)
    artifactNumToKeep(logRotatorArtifactsNumToKeep)
  }
  environmentVariables {
      env('WORKSPACE_NAME',workspaceFolderName)
      env('PROJECT_NAME',projectFolderName)
      env('SCM_URL',scmUrl)
      env('SCM_NAMESPACE',projectScmNamespace)
      env('SCM_CREDENTIAL_ID','adop-jenkins-master')
  }
  label("docker")
  wrappers {
    preBuildCleanup()
    injectPasswords()
    maskPasswords()
    sshAgent("adop-jenkins-master")
  }
  triggers scmProvider.trigger(projectScmNamespace, referenceAppgitRepo, "master")
  definition {
    cpsScm {
      scm {
        git {
          remote { url(referenceAppGitUrl) }
          branches('master')
          scriptPath('Jenkinsfile')
          extensions { }
        }
      }
    }
  }
}
