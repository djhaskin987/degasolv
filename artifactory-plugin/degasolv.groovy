// Plugins Lib Directory
// If your plugin requires any external dependencies, you can place them under
// the ${ARTIFACTORY_HOME}/etc/plugins/lib directory.
// -- https://www.jfrog.com/confluence/display/RTF/User+Plugins
// So, you simply need to put the degasolv jar file there.
// This is why we call clojure from groovy: so that the user's only
// dependency is the degasolv jar itself.

import java.util.Map
import java.util.List
import java.util.HashMap
import java.util.ArrayList
import java.io.ByteArrayInputStream

import groovy.io.FileType

import clojure.java.api.Clojure
import clojure.lang.IFn
import degasolv.resolver.PackageInfo

IFn require = Clojure.var("clojure.core", "require")
require.invoke(Clojure.read("miner.tagged"))
require.invoke(Clojure.read("serovers.core"))
require.invoke(Clojure.read("degasolv.resolver"))
require.invoke(Clojure.read("degasolv.util"))

readString = Clojure.var("miner.tagged", "read-string")
verCmp = Clojure.var("serovers.core", "maven-vercmp")
prStr = Clojure.var("clojure.core", "pr-str")

sortBy = { a, b ->
   versKeyword = Clojure.read(":version")
   return -(verCmp.invoke(a.get(versKeyword), b.get(versKeyword)))
}

List<RepoPath> getDirs(RepoPath dir) {
    return repositories.getChildren(dir)
        .findAll { finfo ->
            finfo.isFolder()
        }
        .collect { finfo ->
            finfo.getRepoPath()
        }
}

List<RepoPath> getFiles(RepoPath dir) {
    return repositories.getChildren(dir)
        .findAll { finfo ->
            !finfo.isFolder()
        }
        .collect { finfo ->
            finfo.getRepoPath()
        }
}

//String getContent(file) {
//    return file.getText("UTF-8")
//}

String getContent(RepoPath p) {
    return p.getContent()
        .getInputStream()
        .getText("UTF-8")
}

Map<String, List<PackageInfo>> mergeRepoInfo(
    Map<String, List<PackageInfo>> a,
    Map<String, List<PackageInfo>> b) {
     b.each{ k, v ->
        if (!a.containsKey(k)) {
            a.put(k, new ArrayList<PackageInfo>())
        }
        lst = a.get(k)
        lst.add(v)
    }
    return a
}

def gatherRepoInfo(RepoPath dir) {

    Map<String, List<PackageInfo>> repoInfo =
        new HashMap<String, List<PackageInfo>>()

    findDSCards = ~/\.dscard$/

    getFiles(dir).each { file ->
        m = (file =~ findDSCards)
        if (m.find()) {
            String cardContents = getContents(file)
            cardData = readString.invoke(cardContents)
            cardID = cardData.get(Clojure.read(":id"))
            cardVersion = cardData.get(Clojure.read(":version"))
            cardLocation = cardData.get(Clojure.read(":location"))

            if (!repoInfo.containsKey(cardID)) {
                repoInfo.put(cardID, new ArrayList<PackageInfo>())
            }
            lst = repoInfo.get(cardID)
            lst.add(cardData)

            // maybe?
            repositories.setProperty(file, "degasolv.id", cardID)
            repositories.setProperty(file, "degasolv.version", cardVersion)
            repositories.setProperty(file, "degasolv.location", cardLocation)
        }
    }
    getDirs(dir).each { cdir ->
        gatherRepoInfo(cdir).each { subRepoInfo ->
            repoInfo = mergeRepoInfo(repoInfo, subRepoInfo)
        }
    }
    return repoInfo
}

def recalculateIndex(RepoPath dir) {
    logger.info("Recalculating degasolv index `" + dir + "`.")

    logger.info("Gathering repository info...")
    repoInfo = gatherRepoInfo(dir)

    logger.info("Done. Sorting repository info...")
    repoInfo.each{ k, v ->
        v.sort(sortBy)
    }

    logger.info("Done. Writing out ``index.dsrepo``...")
    indexPathStr = dir.toPath()
    indexPathStr += (path[-1] == "/" ? "" : "/")
    indexPathStr += "index.dsrepo"

    indexAsStr = prStr.invoke(repoInfo)
    indexStream = new ByteArrayInputStream(indexAsStr.getBytes("UTF-8"))

    indexPath = RepoPathFactory.create(indexPathStr)
    repositories.deploy(indexPath, indexStream)
    logger.info("Done recalculating degasolv index")
}


name = "degasolv"
version = "1.6.0"
description = "Degasolv artifactory plugin for recalculating the ``index.dsrepo`` file."

executions {
  degasolvReindex (version:version, description:description, httpMethod: 'GET', users:[], groups:[], params:[:]) { params ->
      if (! params.get("repo"))
      {
          logger.warning("The ``repo`` key was not given in ``params`` query value, refusing to recalculate an unspecified degasolv repository.")
      }
      else
      {
          repoLoc = params.get("repo")
          repoPath = RepoPathFactory.create(repoLoc)
          if (! repoPath)
          {
               logger.warning("The ``repo`` key was not valid in the ``params`` query value, refusing to recalculate an unspecified degasolv repository.")
          }
          else
          {
              recalculateIndex(repoPath)
          }
      }
   }
}