

#ifndef QUERY_ENTITY_H
#define QUERY_ENTITY_H

#include <string>
#include <vector>




class QueryEntity {

  constexpr static const char *entity_file = "../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json";

public:
  std::string entity_type; // To the enum type
  std::string group;
  std::string target_id;
  std::vector<std::string> aliases;
  

public:
  QueryEntity():
    entity_type(""), group(""), target_id(""), aliases(std::vector<std::string>() ) { }

  QueryEntity(std::string _entity_type, std::string _group, std::string _target_id, std::vector<std::string> a):
    entity_type(_entity_type), group(_group), target_id(_target_id), aliases(a) { }

  std::string toString();

  /** Takes an entity_file and returns the list of query entities */
  static std::vector<QueryEntity> fileToQueryEntity();
  static std::vector<QueryEntity> fileToQueryEntity(std::string fileName);

  static QueryEntity targetidToQueryEntity(std::string targetid);
  static QueryEntity targetidToQueryEntity(std::string targetid, std::vector<QueryEntity> entities);

};


#endif // QUERY_ENTITY_H

