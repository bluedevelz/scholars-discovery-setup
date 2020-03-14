#!/usr/bin/env bash
/app/data-importer/setup_sample_data.py
mvn -f /app/pom.xml clean package -Dmaven.test.skip=true\
       	&& mvn spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.config.location=/app/
