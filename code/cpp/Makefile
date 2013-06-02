#CC = clang++ -std=c++0x
CC = g++ -w -O7 -Wno-deprecated -std=c++0x -march=native
#CC = g++ -g -std=gnu++0x 

#tag = -i

ifdef linux
tag = -n
endif

EXTRAS = -lfl -lboost_program_options -lboost_system -lpthread -lboost_filesystem -lboost_regex -lthrift

main: streamcorpus_types.o streamcorpus_constants.o entity_match.o 
	$(CC) -w -o main streamcorpus_types.o streamcorpus_constants.o entity_match.o $(EXTRAS)

streamcorpus_types.o: streamcorpus_types.cpp
	$(CC) -g -c streamcorpus_types.cpp $(EXTRAS)

streamcorpus_constants.o: streamcorpus_constants.cpp
	$(CC) -g -c streamcorpus_constants.cpp $(EXTRAS)

entity_match.o: entity_match.cpp
	$(CC) -g -c entity_match.cpp $(EXTRAS)


clean:
	rm -f *.o
	rm -f main


