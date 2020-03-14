#!/usr/bin/env bash
tdb_dir=$1
if [[ ! -z ${tdb_dir} ]]; then
  cd /app/data-imported/${tdb_dir}
  if [[ $(pwd) == "/app/data-imported/${tdb_dir}" ]] ; then 
  	rm -rf *
  else       	
    echo "${0} Unable to remove tdb data.  The tdb dir does not exist."
  fi
else
  echo "${0} unable to remove tdb data. Tdb directory not specified."
fi  

