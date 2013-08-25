#CC = clang++ -std=c++0x

GPP = g++
SYS := $(shell gcc -dumpmachine)
ifneq (, $(findstring redhat, $(SYS)))
 GPP = /opt/centos/devtoolset-1.1/root/usr/bin/g++
endif


DEBUG = -g -pg
CC = $(GPP) $(DEBUG) -w -Wno-deprecated -std=c++0x -march=native 
#CC = $(GPP) $(DEBUG) -w -O7 -Wno-deprecated -std=c++0x -march=native 
#CC = g++ -g -std=gnu++0x 

#tag = -i

ifdef linux
tag = -n
endif

#EXTRAS = -lfl -lboost_date_time -lboost_program_options -lboost_system -lpthread -lboost_filesystem -lboost_regex -lthrift -DHAVE_NETINET_IN_H -DHAVE_INTTYPES_H -L/usr/local/lib
EXTRAS = -lfl -lboost_date_time -lboost_program_options -lboost_system -lpthread -lboost_filesystem -lboost_regex -lthrift -DHAVE_NETINET_IN_H -DHAVE_INTTYPES_H 


post_er:  PostSubmissionER.o streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o Word2Vec.o
	$(CC) -w -o post_er PostSubmissionER.o streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o Word2Vec.o $(EXTRAS)

test:  streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o Word2Vec.o
	$(CC) -w -o test test.cc streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o Word2Vec.o $(EXTRAS)

main: streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o Word2Vec.o
	$(CC) -w -o main ER.cc streamcorpus_types.o streamcorpus_constants.o MentionChain.o QueryEntity.o Parameters.o Entity.o Word2Vec.o $(EXTRAS)

PostSubmissionER.o: PostSubmissionER.cc
	$(CC) -c PostSubmissionER.cc $(EXTRAS)

Word2Vec.o: Word2Vec.cc
	$(CC) -c Word2Vec.cc $(EXTRAS)

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
	rm -f post_er


