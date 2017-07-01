// Plugins Lib Directory
// If your plugin requires any external dependencies, you can place them under
// the ${ARTIFACTORY_HOME}/etc/plugins/lib directory.
// -- https://www.jfrog.com/confluence/display/RTF/User+Plugins
// So, you simply need to put the degasolv jar file there.

import java.util.Map
import java.util.List
import java.util.HashMap
import java.util.ArrayList
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

def recalculateIndex(dir) {

    Map<String, List<PackageInfo>> repoInfo =
        new HashMap<String, List<PackageInfo>>()

    findDSCards = ~/\.dscard$/

    baseDir = new File(dir);
    baseDir.eachFileRecurse (FileType.FILES) { file ->
        println file
        m = (file =~ findDSCards)
        if (m.find()) {
            String cardContents = file.getText("UTF-8")
            cardData = readString.invoke(cardContents)
            cardID = cardData.get(Clojure.read(":id"))

            if (!repoInfo.containsKey(cardID)) {
                repoInfo.put(cardID, new ArrayList<PackageInfo>())
            }
            lst = repoInfo.get(cardID)
            lst.add(cardData)
        }
    }
    repoInfo.each{k,v ->
        v.sort(sortBy)
    }

    // for each here that sorts each list using magics

    indexFile = new File('index.dsrepo')
    indexFile.withWriter('UTF-8') { writer ->
        writer.write(prStr.invoke(repoInfo))
    }
}

recalculateIndex("./")
