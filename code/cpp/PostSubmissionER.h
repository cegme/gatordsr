
#ifndef POSTSUBMISSIONER_H
#define POSTSUBMISSIONER_H

#include "MentionChain.h"
#include "QueryEntity.h"

#include <string>
#include <vector>


struct ssf_row {

  std::string team_id;
  std::string system_id;
  std::string stream_id;
  std::string entity_id;
  int confidence;
  int relevance;
  int contains;
  std::string date_hour;
  std::string slot_name;
  std::string slot_value;
  std::string byte_range;

  // std::string sentence; // <-- the sentence context, we wont use this
  void print() {
    printf("%s %s %s %s %d %d %d %s %s %s %s", team_id.c_str(), system_id.c_str(),
      stream_id.c_str(), entity_id.c_str(), confidence, relevance, contains,
      date_hour.c_str(), slot_name.c_str(), slot_value.c_str(), byte_range.c_str());
  }
};

class PostSubmissionER {

private:
  std::vector<QueryEntity> entities;
  // multimap for the related entities
  std::multimap<size_t,size_t> adjmap;

public:
  PostSubmissionER() {
    init();
  }

  // Initializes all data structures needed for ER
  void init();

  // Clusters all the entities that should be clustered
  void initEntityClusters();

  // Given a query entitiy, look for the other matches like it from teh adjmap
  std::vector<size_t> clusterItems(QueryEntity *qe);

  // Here is where the magic happens
  void processSubmissionFile(std::string fileName);

  // Reads a non comment line from the submission file, returns a row struct
  static ssf_row Line2Row(std::string line);

  // Extract the Query Entity from this MentionChain
  static QueryEntity ExtractQueryEntity(ssf_row *row);

  // Extract and build the file name from the ssf_row
  // Ex: "1327982549-85fe459503535923e8b7d6ab8e96877f" -> "85fe459503535923e8b7d6ab8e96877f.gpg"
  static std::string ExtractGPGFile(ssf_row *row);

  // Extract the MentionChain that is refered to by this row
  static MentionChain ExtractMentionChain(ssf_row *row);
  
};


#endif  // POSTSUBMISSIONER_H

