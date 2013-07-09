
#include <boost/property_tree/ptree.hpp>
#include <boost/property_tree/json_parser.hpp>

#include <fstream>
#include <sstream>

#include "QueryEntity.h"



std::string QueryEntity::toString() {
  std::stringstream ss;

  ss << "{\n";
  ss << "\t\"entity_type\": \"" << entity_type << "\",\n";
  ss << "\t\"group\": \"" << group << "\",\n";
  ss << "\t\"target_id\": \"" << target_id << "\",\n";
  ss << "\t\"alias\": [\"" << aliases.front() << "\"" ; // There is at least one alias
  std::for_each(aliases.cbegin()+1, aliases.cend(), [&ss] (std::string a) {
    ss << ", \"" << a << "\"";
  });
  ss << "]\n}";

  return ss.str();
}
  

std::vector<QueryEntity> QueryEntity::fileToQueryEntity(std::string fileName) {

  std::ifstream ifs(fileName, std::ifstream::in);

  boost::property_tree::ptree pt;
  boost::property_tree::json_parser::read_json(fileName, pt);

  std::vector<QueryEntity> qes;

  // Iterate through each target
  for (auto entity : pt.get_child("targets")){
    QueryEntity qe;
    qe.entity_type = entity.second.get<std::string>("entity_type");
    qe.group = entity.second.get<std::string>("group");
    qe.target_id = entity.second.get<std::string>("target_id");
    
    std::vector<std::string> als;
    for (auto &a : entity.second.get_child("alias")) {
      als.push_back(a.second.data());
    }
    qe.aliases = als;
    qes.push_back(qe);
  };

  return qes;
}

