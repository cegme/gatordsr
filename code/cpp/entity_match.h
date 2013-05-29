
#ifndef ENTITY_MATCH_H
#define ENTITY_MATCH_H

//#include <fstream>
#include <iostream>
#include <locale>
#include <sstream>
#include <string>
#include <vector>

namespace streamcorpus {

  struct found_entity {
    std::string targetid;
    std::string group;
    std::string entity_type;
    std::string alias;
  };

  std::vector<found_entity> get_aliases(void) {

    std::locale loc;
    std::ifstream infile("../resources/entity/alias_list.txt");
    std::vector<found_entity> v;

    std::string line;
    while (std::getline(infile, line)) {

      struct found_entity e;
      std::size_t start = line.find("|");
      e.targetid = tolower(line.substr(0,start), loc);
      e.targetid.shrink_to_fit();
      
      std::size_t end = line.find("|",start+1);
      e.group = tolower(line.substr(end, start), loc);
      e.group.shrink_to_fit();
 
      start = end;
      end = line.find("|",start+1);
      e.entity_type = tolower(line.substr(start, end), loc);
      e.entity_type.shrink_to_fit();
    
      start = end;
      e.alias = tolower(line.substr(start+1), loc);
      e.alias.shrink_to_fit();
    
      //std::clog << "e.alias: " << e.alias << std::endl;
      v.push_back(e); 
    }

    v.shrink_to_fit();
    std::clog << "Extracted " << v.size() << " aliases.\n";
    return v;
  }
}



#endif  // ENTITY_MATCH_H

