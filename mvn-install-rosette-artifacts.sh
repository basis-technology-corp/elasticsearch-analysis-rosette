#!/bin/sh -e
# Install jar files needed for the maven build of the elastcisearch-analysis-rosette plugin
# from Basis Technology. 
# ==============================================================================
BT_ROOT=$1
GROUP_ID=com.basistech
#
# Verify that the Basis Technology Root directory exists
#

if [ ! -d "$BT_ROOT" ]; then
  echo
  echo \"$BT_ROOT\" is not a directory.
  echo
  echo "Usage: $0 BT_ROOT"
  echo "       where BT_ROOT is the home directory of the Basis Technology Rosette SDK." 
  echo "  e.g. $0 /opt/rlp_7.9"
  echo
  exit 1
fi
VER=20
FILE=$(find $BT_ROOT -name btcommon-$VER.jar)
mvn install:install-file -DgroupId=$GROUP_ID -DartifactId=common -Dversion=$VER -Dfile=$FILE -Dpackaging=jar -DgeneratePom=true

VER=7.9.1
FILE=$(find $BT_ROOT -name btrlp.jar)
mvn install:install-file -DgroupId=$GROUP_ID -DartifactId=rlp -Dversion=$VER -Dfile=$FILE -Dpackaging=jar -DgeneratePom=true

FILE=$(find $BT_ROOT -name btutil.jar)
mvn install:install-file -DgroupId=$GROUP_ID -DartifactId=utilities -Dversion=$VER -Dfile=$FILE -Dpackaging=jar -DgeneratePom=true

FILE=$(find $BT_ROOT -name btrlplucene-L43S43-7.9.jar)
GROUP_ID=$GROUP_ID.rlp.lucene
mvn install:install-file -DgroupId=$GROUP_ID -DartifactId=rlp-lucene-solr-43 -Dversion=$VER -Dfile=$FILE -Dpackaging=jar -DgeneratePom=true

FILE=$(find $BT_ROOT -name btrlpextra-7.9.jar)
mvn install:install-file -DgroupId=$GROUP_ID -DartifactId=rlp-lucene-extra -Dversion=$VER -Dfile=$FILE -Dpackaging=jar -DgeneratePom=true
