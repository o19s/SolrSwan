# Simple bash script that uses git commands to determine the version of the 
# software and write it to a java properties file for later use.
#!/bin/bash
revisioncount=`git log --oneline | wc -l`
projectversion=`git describe --tags --long`
cleanversion=${projectversion%%-*}
gitsha=`git rev-list --tags --max-count=1`

echo "version=$cleanversion.$revisioncount" > src/main/resources/version.properties
echo "date=`date +%F`" >> src/main/resources/version.properties
echo "git=$gitsha" >> src/main/resources/version.properties

