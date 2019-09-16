(require '[clj-http.client :as client]
         '[cheshire.core :as json])

(defn all-tags [org]
  (let
    [baby (client/post
    "https://api.github.com/graphql"
    {:basic-auth ["djhaskin987" "77907ab111d65f085e2e80a79227d53bcf11c000"]
     :content-type :json
     :accept :json
     :cookie-policy :standard
     :body
     (json/generate-string
       {:variables
        {:login "djhaskin987"
         }
        :query
        (clojure.string/join
          "\n"
          [
           "query RepoStuff($login: String!) {"
           "  rateLimit {"
           "    cost"
           "    remaining"
           "    resetAt"
           "  }"
           "  repositoryOwner(login: $login) {"
           "    repositories(first: 100) {"
           "      edges {"
           "        cursor"
           "        node {"
           "          name"
           "          refs(refPrefix: \"refs/tags/\", first: 100) {"
           "            edges {"
           "              cursor"
           "              node {"
           "                name"
           "                target {"
           "                  oid"
           "                  ... on Tag {"
           "                    name"
           "                    message"
           "                  }"
           "                }"
           "              }"
           "            }"
           "          }"
           "        }"
           "      }"
           "    }"
           "  }"
           "}"
           ])})}))
