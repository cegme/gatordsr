
#ifndef ENTITY_MATCH_H
#define ENTITY_MATCH_H

//#include <fstream>
#include <iostream>
#include <locale>
#include <sstream>
#include <string>
#include <regex>
#include <vector>
#include <boost/algorithm/string.hpp>    
#include <boost/regex.hpp>

#include "Util.h"

namespace streamcorpus {

  struct found_entity {
    std::string targetid;
    std::string group;
    std::string entity_type;
    std::string alias;
    //boost::regex alias_regex;
    found_entity() {}
    found_entity(std::string _t, std::string _g, std::string _e, std::string _a): 
      targetid(_t), group(_g), entity_type(_e), alias(_a) {
        // Keep the aliases lowercase
        boost::algorithm::to_lower(alias);
        //init_regex();
      }
    /*inline void init_regex() {
      std::string r("\\b" + alias + "\\b");
      alias_regex = r;
    }*/
  };

  std::vector<found_entity> get_aliases() {

    std::locale loc;
    std::ifstream infile("../resources/entity/alias_list_6_11.txt");
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
      //e.init_regex();
      boost::algorithm::to_lower(e.targetid);
      boost::algorithm::to_lower(e.group);
      boost::algorithm::to_lower(e.entity_type);

      v.push_back(e); 
    }

    //std::clog << "Extracted " << v.size() << " aliases.\n";
    return v;
  }

  struct HasEntity {
    std::string s;
    HasEntity(std::string _s): s(_s) {
      boost::algorithm::to_lower(s);
    }
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

