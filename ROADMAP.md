# MVP
1. Validate dscard files and print good error on generate-repo, and in
   resolve-locations when reading index.dsrepo
2. Use JSON for dscard and index.dsrepo, modify error message logic to
   be descriptive even when checking the JSON
3. Support JSON project files with resolve-locations
4. Document basic tutorial on how to use it.
# 1.2
1. Spec and generative test .resolver
2. Set up autodocs and tutorial merger (sphinx)
3. Put docs on readthedocs
4. packr
5. Pluggable config file
6. inline-repository-data (using string reqs)
# 1.3
1. Better error message on unresolve
