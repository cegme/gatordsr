
#include <boost/property_tree/json_parser.hpp>
#include <boost/property_tree/ptree.hpp>
#include <boost/algorithm/string.hpp>

#include <fstream>
#include <sstream>

#include "QueryEntity.h"
#include "Util.h"



std::string QueryEntity::pretty_string() const {
  std::string s = toString();
  boost::replace_all(s, "\n", " ");
  return s;
}
std::string QueryEntity::toString() const {
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
  

std::vector<QueryEntity> QueryEntity::fileToQueryEntity() {
  return QueryEntity::fileToQueryEntity(QueryEntity::entity_file);
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
    qe.init();
    qes.push_back(qe);
  };

  return qes;
}


QueryEntity QueryEntity::targetidToQueryEntity(std::string target_id) {

  auto entities = QueryEntity::fileToQueryEntity(QueryEntity::entity_file);

  for(auto e: entities) {
    if (target_id == e.target_id) {
      //std::cerr << "Found one: " << target_id << "\n";
      return e;
    }
  }
  std::cerr << "Error fileToQueryEntity: Could not find a query entity for target_id: " << target_id << "\n";
  return QueryEntity();
}


QueryEntity QueryEntity::targetidToQueryEntity(std::string target_id, std::vector<QueryEntity> entities) {

  for(auto e: entities) {
    if (target_id == e.target_id) {
      //std::cerr << "Found one: " << target_id << "\n";
      return e;
    }
  }
  log_err("Error fileToQueryEntity: Could not find a query entity for target_id: ", target_id.c_str());
  return QueryEntity();
}


std::string QueryEntity::getWikiBody(const char *page_title) {

  std::stringstream ss;
  
  // Fetch the string of the page given in the page title
  char tmpfilenam[128];
  char cmd[512];

  // Create a tmp file name for the webpage
  std::tmpnam(tmpfilenam);
  //log_debug("tmpfilenam: %s", tmpfilenam);
  
  std::snprintf(cmd, 512, wiki_page, tmpfilenam, page_title);
  log_debug("cmd: %s", cmd);

  // Call the wikipedia api
  system(cmd); // FIXME dangerous!

  std::ifstream ifs(tmpfilenam, std::ifstream::in);

  boost::property_tree::ptree pt;
  boost::property_tree::json_parser::read_json(tmpfilenam, pt);

  
  //log_info("page: %s", page.c_str());
  //return page;
  // Example path for Stuart_Powell_Field is "query.page.9471818.extract"
  //log_debug("pt.get_child: %s", pt.get_child("query.pages").data().c_str());
  for (auto page : pt.get_child("query.pages")){
    if (page.first == "-1") {
      // Invalid result, ignore it
      log_err ("Invalid result for page_title: %s", page_title);
      continue; 
    }
    //log_debug("page.first: %s", page.first.c_str());
    //log_debug("page.second: %s", page.second.data().c_str());
    ss <<  page.second.get<std::string>("extract");
  }
  return ss.str();
}


std::string QueryEntity::targetIdToTitle(std::string target_id) {
  auto slash = target_id.find_last_of("/");
  return target_id.substr(slash+1);
}


void QueryEntity::init() {

  // Tokenize the text of the entity and make it a property

  // Wikipedia links
  if (target_id.find("wiki") != std::string::npos) { // FIXME make a better wikipedia checker
    std::string title = QueryEntity::targetIdToTitle(target_id);
    std::string wikipedia_page(QueryEntity::getWikiBody(title.c_str()));

    std::vector<std::string> tokens;
    boost::split(tokens, wikipedia_page, boost::is_any_of(", "), boost::token_compress_on);
    wikipedia_tokens = tokens;
  }

}


