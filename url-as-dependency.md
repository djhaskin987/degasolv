a -> b
a -> c: "cs-url"



turns into
a == 1.0
  a -> b
  a -> c==cs-url(-hash?) how can I guarantee the uniqueness of this thing?  
b == 1.0
c == cs-url(-hash?) @ cs-url

c == cs-urls-hash @ cs-url, no deps

what is a "tolerant" version comparison algorithm?
  - capable of comparing *any* unicode string with any other
  - Makes sense when the input makes sense
  - when it makes less sense, version alg does its level best to make sense of it
  - perhaps returns a warning, but does not error

I should start to do generative testing on serovers!
