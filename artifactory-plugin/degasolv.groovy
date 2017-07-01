
import clojure.java.api.Clojure
import clojure.lang.IFn

def recalculateIndex(dir) {
    IFn require = Clojure.var("clojure.core", "require")
    require.invoke(Clojure.read("degasolv.pkgsys.core"))
    IFn generateRepoIndex = Clojure.var("degasolv.pkgsys.core",
        "generate-repo-index!")
    generateRepoIndex.invoke(dir, dir + "/index.dsrepo", null);
}

download {
    /**
     * Artifactory caches remote artifacts after initial download. By default,
     * Artifactory doesn't recognize repodata.json as metadata that must be
     * expired, rather than cached.
     */
    beforeDownloadRequest { Request request, RepoPath repoPath ->
        if (repositories.getRemoteRepositories().contains(repoPath.getRepoKey())) {
            if (repoPath.getName().equalsIgnoreCase("index.dsrepo")) {
                log.warn("Expiring repodata at " + repoPath)
                    expired = true
            }
        }
    }
}



/**
 * Support for local repositories as conda channels.
 */
jobs {
    /**
     * Every 10 seconds, index all local repositories that have the "conda" property.
     */
    indexPackages(delay: 0, interval: 10000) {
        log.info("Executing indexPackages")
        long start = System.currentTimeMillis()
        for (RepoPath repo : getCondaRepos()) {
            indexPackagesRecursive(repo)
        }
        long end = System.currentTimeMillis()
        log.info("Took " + (end-start) + "ms to execute indexPackages")
    }
}


recalculateIndex("./")
