

#ifndef QUERY_ENTITY_H
#define QUERY_ENTITY_H

#include <string>
#include <vector>




class QueryEntity {

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

  static std::vector<QueryEntity> fileToQueryEntity(std::string fileName);

};


#endif // QUERY_ENTITY_H

