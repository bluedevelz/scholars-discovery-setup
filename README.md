# Quick runner/wrapper for scholars-discovery project

## Instructions:

### If Git source code management is not already installed on your system
[Download and install Git](https://git-scm.com/downloads) 

### If Docker is not installed on your system use the information for installing on your platform [here](https://docs.docker.com).

### Ensure that [docker-compose is installed](https://docs.docker.com/compose/install/)

## Clone this repository
from a command line...
1) change the current directory to a directory where this repository should be stored on your local machine
2) git clone https://github.com/vivo-community/scholars-discovery-docker-setup.git
3) change directory to the new scholars-discovery-docker-setup directory created by git

## Import samples git repositories and the scholars-discovery repository into the base folder of this repository
from the command line in the scholars-disovery-docker-setup directory created above...

1) 'git clone https://github.com/mconlon17/vivo-sample-data-generator.git' 
    generated data. May be customized by adapting the included python code [Author's README](https://github.com/mconlon17/vivo-sample-data-generator/blob/master/README.md)
2) 'git clone https://github.com/vivo-project/sample-data.git'
    sample data from open-vivo and Florida state
3) 'git clone https://github.com/vivo-community/scholars-discovery.git'
    The TDB data extractor, Solr index and GraphQL endpoint 

NOTE: everything assumes the scholars-discovery code is checked out into the 
`scholars-discovery` directory. It is in the `.gitignore` file.

This will run off the `scholars-discovery` master branch.  Right now that might
not have a grapqhl endpoint - for the time being it may be best to switch to
the `vstf-staging` branch (e.g. `git checkout ...`).  The idea for this
project is you should be able to use it to run *any* branch in development mode.


## Initial setup
By default, running the 'docker-compose up' will create a new TDB for the openvivo sample data and clear the Solr index before loading. See the infrmation below in the section 'Sample Query' about running GraphQL queries.

##  After initial execution (re-runs)
The operation of the setup is primarily controlled by 4 variables in the application-dev.yml file
in the base directory of this project (scholars-discovery-docker-setup). The controls affect the following
features:
1) Whether the Solr index is re-indexed when the containers are (re-)started
   middleware.index.onStartup: true | false

2) Whether the Solr index ls cleared before pulling data from the TDB (either supplied by the user or built from the samples (repos 1 and 2 above in the 'Import samples git repositories' step) - This option should be set for new TDBs, or when changing TDBs
   middleware.index.clearOnStartup: true | false

3) Whether to create the TDB from sample data (specific samples are built depending on the TDB name supplied as option 3)
   middleware.index.createFromSampleData: true | false

4) The directory containing the TDB (either built or user supplied)
  If user-supplied i.e. a TDB generated outside this program, it must be copied into a directory under ./data-imported and the subdirectory should not be named 'generated', 'openvivo', 'florida' or 'duke' as these may be overwritten by the samples if 'middleware.triplestore.createFromSampleData' is set to true.
  NOTE: in the docker container ./data-imported is mapped to /data, so the TDB directory settings are /data/MY_CUSTOM_TDB (for example as a user supplied TDB directory),  /data/generated (generated sample data), /data/florida (University of Florida sample data), or /data/openvivo (OpenVivo sample data). NOTE: /data/duke data is not supplied in the samples. 
  If set to one of the above values /data/generated, /data/openvivo, /data/florida then createFromSampleData will load the specifed samples from either the vivo-sample-data-generator.  If createFromSampleData is set to true for a user supplied TDB, errors will occur.
NOTE: After the initial run, the values for onStartup, clearOnStartup and createFromSampleData should be set to false in most cases unless it's necessary to change TDB.

##  Re-running and other scenarios

### Re-running the containers
1) In another command window, change directory to the scholars-discovery-docker-setup directory and use the command 'docker-compose down' to stop the containers currenly running
2) If no TDB changes are required, ensure the following settings in application-dev.yml:
   middleware.index.onStartup: false
   middleware.index.clearOnStartup: false
   middleware.triplestore.createFromSampleData: false

3) In the original window, use 'docker-compose up' to start the containers
NOTE: Required when changing TDB or changing application-dev.yml parameters

### User supplied TDB
1) Stop the containers (docker-compose down)
2) Put TDB data in the data-imported/MY_TDB_NAME directory (create the directory if necessary).  Then ensure the current directory is the scholars-discovery-setup directory.
3) Make adjustments to the application-dev.yml to point the system to the target TDB 
   middleware.index.onStartup: true
   middleware.index.clearOnStartup: true
   middleware.triplestore.directory: /data/MY_TDB_NAME
   middleware.triplestore.createFromSampleData: false  ## since no sample data is involved and does not need to be loaded
3) `docker-compose up`

### Creating a TDB from one of the supplied sample datasets
1) Stop the containers (docker-compose down)
2) Make adjustments to the application-dev.yml to point the system to the target TDB 
   middleware.index.onStartup: true
   middleware.index.clearOnStartup: true
   middleware.triplestore.directory: /data/SAMPLE_DS_NAME SAMPLE_DS_NAME may be one of openvivo, florida or generated
   middleware.triplestore.createFromSampleData: false  ## since no sample data is involved and does not need to be loaded
3) `docker-compose up`


This is making us of a file `application-dev.yml` to configure `scholars-discovery`
for local development mode.  It also defaults to importing data into the index 
at startup.

However, you can change `middleware.index.onStartup=false` **after the first start up** 
has finished and imported into the index succesfully - so that it doesn't rebuild the
index every time.

```yaml
# file: application-dev.yml

...
middleware:
  index:
    onStartup: true # change to false after first run
...

```

## Sample Query

Once you have running, can go this http://localhost:9000/gui and run GraphQL queries.
Here is a sample one:

```graphql
query  {
  people (
    facets: [
      {field: "name" },
      {field: "region"},
    ],
    #filters: [{field: "region", value: "NC"}]
    paging: { pageSize:10, pageNumber: 0
    },
    query: "*"
  ) {
    content {
      id
      name
      keywords
      positions {
        title
      }
      publications {
        title
      }
    }
    page {
        totalElements
        totalPages
        number
        size
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

