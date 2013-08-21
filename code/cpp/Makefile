#CC = clang++ -std=c++0x

GPP = g++
SYS := $(shell gcc -dumpmachine)
ifneq (, $(findstring redhat, $(SYS)))
 GPP = /opt/centos/devtoolset-1.1/root/usr/bin/g++ -L/usr/local/lib/ -lthrift-1.0.0-dev
endif


DEBUG = -g -pg
CC = $(GPP) $(DEBUG) -w -O7 -Wno-deprecated -std=c++0x -march=native 
#CC = g++ -g -std=gnu++0x 

#tag = -i

ifdef linux
tag = -n
endif

#EXTRAS = -lfl -lboost_date_time -lboost_program_options -lboost_system -lpthread -lboost_filesystem -lboost_regex -lthrift -DHAVE_NETINET_IN_H -DHAVE_INTTYPES_H -L/usr/local/lib
EXTRAS = -lfl -lboost_date_time -lboost_program_options -lboost_system -lpthread -lboost_filesystem -lboost_regex -lthrift -DHAVE_NETINET_IN_H -DHAVE_INTTYPES_H 

main: streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o
	$(CC) -w -o main ER.cc streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o $(EXTRAS)

test:  streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o
	$(CC) -w -o test test.cc streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o $(EXTRAS)


#MentionChain: streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o
#	$(CC) -w -o main streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o $(EXTRAS)

#entity: streamcorpus_types.o streamcorpus_constants.o entity_match.o MentionChain.o
#	$(CC) -w -o entity streamcorpus_types.o streamcorpus_constants.o entity_match.o MentionChain.o $(EXTRAS)

streamcorpus_types.o: streamcorpus_types.cpp
	$(CC) -c streamcorpus_types.cpp $(EXTRAS)

streamcorpus_constants.o: streamcorpus_constants.cpp
	$(CC) -c streamcorpus_constants.cpp $(EXTRAS)

entity_match.o: entity_match.cpp
	$(CC) -c entity_match.cpp $(EXTRAS)

MentionChain.o: MentionChain.cc
	$(CC) -c MentionChain.cc $(EXTRAS)

QueryEntity.o: QueryEntity.cc
	$(CC) -c QueryEntity.cc $(EXTRAS)

Parameters.o: Parameters.cc
	$(CC) -c Parameters.cc $(EXTRAS)

Entity.o: Entity.cc
	$(CC) -c Entity.cc $(EXTRAS)

ER.o: ER.cc
	$(CC) -c ER.cc $(EXTRAS)


clean:
	rm -f *.o
	rm -f main
	rm -f test


