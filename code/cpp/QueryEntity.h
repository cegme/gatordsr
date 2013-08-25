

#ifndef QUERY_ENTITY_H
#define QUERY_ENTITY_H

#include <string>
#include <vector>


class QueryEntity {

public:
  constexpr static const char *entity_file = "../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json";
  constexpr static const char *wiki_page = "wget -q -O %s 'http://en.wikipedia.org/w/api.php?action=query&prop=extracts&explaintext=&exsectionformat=plain&format=json&titles=%s' ";

  std::string entity_type; // To the enum type
  std::string group;
  std::string target_id;
  std::vector<std::string> aliases;
  std::vector<std::string> wikipedia_tokens; // TODO keep? add an init() function?


public:
  QueryEntity():
    entity_type(""), group(""), target_id(""), aliases(std::vector<std::string>()),wikipedia_tokens(std::vector<std::string>() ) { }

  QueryEntity(std::string _entity_type, std::string _group, std::string _target_id, std::vector<std::string> a, std::vector<std::string> _wikipedia_tokens):
    entity_type(_entity_type), group(_group), target_id(_target_id), aliases(a), wikipedia_tokens(_wikipedia_tokens) { }

  std::string toString() const;
  std::string pretty_string() const;

  std::string name() const {
    return target_id.substr(target_id.find_last_of("/"));
  }

  /** Takes an entity_file and returns the list of query entities */
  static std::vector<QueryEntity> fileToQueryEntity();
  static std::vector<QueryEntity> fileToQueryEntity(std::string fileName);

  static QueryEntity targetidToQueryEntity(std::string targetid);
  static QueryEntity targetidToQueryEntity(std::string targetid, std::vector<QueryEntity> entities);
  static QueryEntity fileToQueryEntity(std::string fileName, std::string target_url) ;
  static QueryEntity UrlToQueryEntity(std::string target_url) ; // ex param: http://en.wikipedia.org/wiki/Buddy_MacKay

  /**
    * Extracts the contets of a wikipedia page with the given title.
    *
    * Usage: QueryEntity::getWikiBody("Stuart_Powell_Field");
    *
    */
  static std::string getWikiBody(const char *page_title);

  /**
    * Takes the target_id which is a link and returns the title. The title 
    * is the string after the last '/'.
    */
  static std::string targetIdToTitle(std::string target_id);

  /**
    * Initialize all the expensive members. Such as tokens.
    */
  void init();

};


#endif // QUERY_ENTITY_H

