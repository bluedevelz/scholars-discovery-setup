# Quick runner/wrapper for scholars-discovery project

Instructions:

1) git clone https://github.com:vivo-community/scholars-discovery.git 
2) git clone https//github.com:vivo-project/sample-data.git

NOTE: everything assumes the scholars-discovery code is checked out into the 
`scholars-discovery` directory, and `sample-data` into a
`sample-data` directory

NOTE: the first time you run this - do NOT run `docker-compose` right away

If you have **already imported** data (see below) can just run:

3) `docker-compose up`

**else** (import some data)

3) `cd data-importer`
4) `./gradlew build` (or `gradlew.bat` on Windows)
5) `./gradlew run --args='openvivo --import'`

That should (after 5-10 minutes) put data in the data-imported/openvivo
directory.  Then 

6) `cd ..`
7) `docker-compose up`

You can also change this to false **after the first start up** has finished
and imported into the index succesfully

```yaml
middleware:
  index:
    onStartup: true
```




