
1. Clear out the NAS directory:
   ```
   rm -f nas/*
   ```

1. Kill any lighttpd procs:
   ```
   pkill lighttpd
   ```

1. Start the screenshot

1. Show the degasolv script in the file
   `~/.local/bin/degasolv`.

1. Start the NAS http server:
   ```
   lighttpd -D -f lighttpd.conf 2>&1 >/dev/null &
   ```

1. Go into the `liblegacy` project:
   ```
   cd liblegacy
   ```

1. Edit the source files `legacy.h` and `legacy.c`

1. Edit the `./build-legacy` script
  1. Pause at each block to show what is happening

1. Edit the `./degasolv.edn` file
  1. Pause at each block to show what is happening

1. Edit the `./CMakeLists.txt` file
  1. When pausing, show that there's nothing special here

1. Edit the degasolv.edn file

1. Run the build-legacy script
   ```
   ./build-legacy
   ```

1. Change directory to the nas folder and show its contents:
   ```
   cd ../nas
   ls -lah
   ```

1. Show the contents of the zip file:
   ```
   unzip -l ./liblegacy-1.0.zip
   ```

1. Show the zip file and card file in the browser

1. Change directory into libshiny:
   ```
   cd ../libshiny
   ```

1. Edit the ./build-shiny script, pausing on each block:
   ```
   vim ./build-shiny
   ```

1. Edit the source files `shiny.h` and `shiny.c`

1. Edit the degasolv.edn file

1. Run the ./build-shiny script

1. Show the contents of the zip file:
   ```
   unzip -l ./libshiny-1.3.0.zip
   ```

1. Show the contents of the NAS using the browser

1. Go into `../pyfrontend`

1. Edit `./build-pyfrontend`

1. Edit the degasolv.edn file

1. Edit `./pyfrontend.py` (the pledge)

1. Run the `./build-pyfrontend` script

1. Change directory to the nas folder and show its contents:
   ```
   cd ../nas
   ls -lah
   ```

1. Show the contents of the zip file:
   ```
   unzip -l ./pyfrontend-2.0.zip
   ```

1. Show the zip file and card file in the browser

1. Query the repo:
   ```
   degasolv query-repo -q pyfrontend
   ```
