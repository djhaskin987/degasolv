(ns dependable.install)

(defn install!
  "Resolves the given specs using the given resolver and repo.
   Downloads the packages, and unpacks them.
   Records what files were installed, and from what packages."
  [project-root specs resolver repo]
  nil)

(defn unpack!
  "Downloads and attempts to unpack the package at the given URL.
   It is an error for the URL to be not a valid package.
   It is an error for the URL to be unresolvable."
  [url]
  nil)


  
  
