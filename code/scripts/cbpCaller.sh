#!/bin/bash
echo "Bash version ${BASH_VERSION}..."
echo "gatordsr code runner for TREC KBA 2013. This will run multiple instances of the main project, namely 32 instances on a 32 core machine."

for i in {0..1}
  do
     echo "Welcome $i times"
     env JAVA_OPTS="-Xmx2g" sbt 'run-main edu.ufl.cise.CorpusBatchProcessor $i' 1> ~/trec/runs/run${i}Log.txt & 2> ~/trec/runs/run${i}Err.txt
 done

