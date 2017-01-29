# CLI Tutorial (under contruction)

so,

```clojure
{
    :id "a"
    :version "2.3.0"
    :artifact-name "a-2.3.0.zip"
    :location "http://example.com/repo/a-2.3.0.zip"
    :sha256-sum "1cacb8cf7b1e00481019a4d76ba2b1c9511e11055329afd28272873d0a41499c"
    :requirements 
        [[{:id "b"
           :status :present
           :spec [[{:relation :greater-equal
                    :version "0.3.0"}]]}]]
}
```

```clojure
{
    :id "b"
    :version "0.4.0"
    :artifact-name "b-3.2.4.zip"
    
    
    
