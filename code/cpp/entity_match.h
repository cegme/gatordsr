
#ifndef ENTITY_MATCH_H
#define ENTITY_MATCH_H

//#include <fstream>
#include <iostream>
#include <locale>
#include <sstream>
#include <string>
#include <vector>
#include <boost/algorithm/string.hpp>    


namespace streamcorpus {

  struct found_entity {
    std::string targetid;
    std::string group;
    std::string entity_type;
    std::string alias;
  };

  std::vector<found_entity> get_aliases() {

    std::locale loc;
    std::ifstream infile("../resources/entity/alias_list.txt");
    std::vector<found_entity> v;

    std::string line;
    while (std::getline(infile, line)) {

      struct found_entity e;
      std::size_t start = line.find("|");
      e.targetid = line.substr(0,start);
      //e.targetid = tolower(line.substr(0,start), loc);
      
      std::size_t end = line.find("|",start+1);
      e.group = line.substr(end, start);
 
      start = end;
      end = line.find("|",start+1);
      e.entity_type = line.substr(start, end);
    
      start = end;
      e.alias = line.substr(start+1);
    
      boost::algorithm::to_lower(e.alias);
      boost::algorithm::to_lower(e.targetid);
      boost::algorithm::to_lower(e.group);
      boost::algorithm::to_lower(e.entity_type);

      v.push_back(e); 
    }

    //std::clog << "Extracted " << v.size() << " aliases.\n";
    return v;
  }

  struct HasEntity {
    const std::string s;
    HasEntity(std::string _s): s(_s){}
    bool operator()(const found_entity &v) const {
      //return v.alias.find(s) != std::string::npos;
      return s.find(v.alias) != std::string::npos;
    }
  };

   /**
    * I had to create my own any_of because centos has an
    * old version of gcc
    */
  template< class InputIt, class UnaryPredicate >
    bool any_of(InputIt first, InputIt last, UnaryPredicate p) {

    for (; first != last; ++first) {
      if (p(*first)) return true;
    }
    return false;
  }

}



#endif  // ENTITY_MATCH_H

