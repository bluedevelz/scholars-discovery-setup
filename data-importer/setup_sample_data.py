#!/usr/bin/env python
import yaml
import os
import pathlib
from time import sleep
func = __file__

print(func + " is executing.")

def mapDirNameToSampleSet(dirName):
    rv = None
    dirL = dirName.lower()
    if 'duke' in dirL:
        rv = 'duke'
    elif 'openvivo' in dirL:
        rv = 'openvivo'
    elif 'florida' in dirL:
        rv = 'florida'
    elif 'generated' in dirL:
        rv = 'generated'
    return rv

index_on_startup = None
tdb_dir = None
create_from_sample_data = None
yaml_data = None

try:
  stream = open('/app/application-dev.yml', 'r')  
  yaml_data = yaml.load(stream) 
  stream.close()

  solr_host = yaml_data['spring']['data']['solr']['host']
  index_on_startup = yaml_data['middleware']['index']['onStartup']
  clear_on_startup = yaml_data['middleware']['index']['clearOnStartup']
  tdb_dir = yaml_data['middleware']['triplestore']['directory']
  create_from_sample_data = yaml_data['middleware']['triplestore']['createFromSampleData']

  print(func + ' - Index SOLR DB on startup: ' + str(index_on_startup))
  print(func + ' - TDB directory: ' + tdb_dir)
  print(func + ' - Create sample data: ' + str(create_from_sample_data))

except ImportError:
  print(func + ' - YAML import error in setup_sample_data reading application-dev.yml')

if (not index_on_startup == None and not tdb_dir == None and not yaml_data == None) :
    if clear_on_startup:
        print(func + ' - waiting for Solr to stabilize...', flush=True) # TODO capture response 503 from curl and loop until solr ready
        sleep(10)
        print(func + ' - Done waiting for Solr to stabilize.', flush=True)
        print(func + ' - WARNING: clearing Solr data (clearOnStartup is true)', flush=True)
        curlUrl = solr_host
        curlUrl += '/scholars-discovery/update?commitWithin=1000&overwrite=true&wt=json'
        curlCmd = 'curl --header "Content-Type: application/xml" --request POST --retry 3 --retry-connrefused --retry-delay 2 '
        curlCmd += ' --data "<add><delete><query>*:*</query></delete></add>" '
        curlCmd += curlUrl
        print(func + 'executing; ' + curlCmd, flush=True)
        os.system(curlCmd)
        print(func + ' - Solr data cleared', flush=True)
    if create_from_sample_data:
        sample_type = mapDirNameToSampleSet(tdb_dir)
        if not sample_type == None:
          if sample_type == 'generated':
              print(func + ' - generating data using vivo-sample-data-generator')
              print(func + ' - creating TDB from sample data. For sample type: ' + sample_type, flush=True)
              os.system('cd /app/generated-data; python sample-data-generator.py') # quick and dirty TODO fix usage of os.system 
              pathlib.Path('/data-imported/generated').mkdir(parents=True, exist_ok=True)
              os.system('/app/data-importer/remove-tdb-data.sh generated')
              os.system('cp /app/generated-data/sample-data.ttl /app/data-imported')
              os.system("cd /app/data-importer;./gradlew build;./gradlew run --args='generated --import'")
          else:
              print(func + ' - importing sample data from ' + sample_type + ' project', flush=True)
              os.system('/app/data-importer/remove-tdb-data.sh ' + sample_type)
              os.system("cd /app/data-importer;./gradlew build;./gradlew run --args='" + sample_type + " --import'")
        else:
          print(func + ' - UNABLE to create TDB from sample data. Cannot identify type from application-dev.yml triplestore directory', flush=True)
    else:
        print(func + ' - bypassing creation of TDB from sample data.', flush=True) 
else:
  print(func + ' - error reading application-dev.yml - not generating sample data', flush=True)


