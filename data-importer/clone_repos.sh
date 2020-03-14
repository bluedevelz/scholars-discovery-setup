#!/usr/bin/env bash
echo Downloading required repos
download_repo () {
  repo=$1
  dir=$2  
  if [[ ! -d ${dir} ]]; then
    git clone ${repo}
  else 
    echo "${repo} previously downloaded. No action taken."
  fi
}
download_repo https://github.com/mconlon17/vivo-sample-data-generator.git vivo-sample-data-generator
download_repo https://github.com/vivo-community/scholars-discovery.git scholars-discovery
download_repo https://github.com/vivo-project/sample-data.git sample-data
echo Downloads complete

