# Quick runner/wrapper for scholars-discovery project

Instructions:

1) git clone https://github.com/vivo-community/scholars-discovery.git 
2) git clone https://github.com/vivo-project/sample-data.git

NOTE: everything assumes the scholars-discovery code is checked out into the 
`scholars-discovery` directory, and `sample-data` into a `sample-data` directory
(which is the default anyway)

This will run off the `scholars-discovery` master branch.  Right now that might
not have a grapqhl endpoint - for the time being it may be best to switch to
the `vstf-sprint6-staging` branch (e.g. `git checkout ...`)

### import some data

3) `cd data-importer`
4) `./gradlew build` (or `gradlew.bat` on Windows)
5) `./gradlew run --args='openvivo --import'`

That should (after 5-10 minutes) put data in the data-imported/openvivo
directory.  Then `cd ..` (back up to scholars-discovery-setup directory)

6) `cd ..`
7) `docker-compose up`

### or (if already imported data into TDB)

3) `docker-compose up`

### 
You can also change this to false **after the first start up** has finished
and imported into the index succesfully

```yaml
middleware:
  index:
    onStartup: true # change to false after first run
```

## Sample Query

Once you have running, can go this http://localhost:9000/gui and run GraphQL queries:

```graphql
query {
  personsFacetedSearch(
    facets: [{field: "keywords"}],
    filters: [{field: "preferredTitle", value:"Software Developer"}]
    paging: { pageSize:100, pageNumber: 0},
    query: "*",
  ) {
    content {
      id
      name
      keywords
      preferredTitle
      positions {
        organizations {
          label
        }
      }
    }
    page {
      totalPages
      number
      size
      totalElements
    }
    facets {
      field
      entries {
        content { 
          value
          count 
        }
      }
    }
  }
}
```

