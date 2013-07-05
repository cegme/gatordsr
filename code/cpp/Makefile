#CC = clang++ -std=c++0x
DEBUG = -g
CC = g++ $(DEBUG) -w -O7 -Wno-deprecated -std=c++0x -march=native 
#CC = g++ -g -std=gnu++0x 

#tag = -i

ifdef linux
tag = -n
endif

EXTRAS = -lfl -lboost_program_options -lboost_system -lpthread -lboost_filesystem -lboost_regex -lthrift -DHAVE_NETINET_IN_H

main: streamcorpus_types.o streamcorpus_constants.o MentionChain.o
	$(CC) -w -o main streamcorpus_types.o streamcorpus_constants.o MentionChain.o $(EXTRAS)

entity: streamcorpus_types.o streamcorpus_constants.o entity_match.o MentionChain.o
	$(CC) -w -o entity streamcorpus_types.o streamcorpus_constants.o entity_match.o MentionChain.o $(EXTRAS)

streamcorpus_types.o: streamcorpus_types.cpp
	$(CC) -c streamcorpus_types.cpp $(EXTRAS)

streamcorpus_constants.o: streamcorpus_constants.cpp
	$(CC) -c streamcorpus_constants.cpp $(EXTRAS)

entity_match.o: entity_match.cpp
	$(CC) -c entity_match.cpp $(EXTRAS)

MentionChain.o: MentionChain.cc
	$(CC) -c MentionChain.cc $(EXTRAS)

clean:
	rm -f *.o
	rm -f main


