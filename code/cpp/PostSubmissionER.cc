
#include "PostSubmissionER.h"

#include "streamcorpus_types.h"
#include "Util.h"
#include "Word2Vec.h"

#include <algorithm>
#include <fstream>
#include <sstream>


void PostSubmissionER::init () {

  // Finitialize 
 adjmap = std::multimap<size_t,size_t>(); 

  // Initialize all the entities
  log_info("Initializing all entities...");
  entities = QueryEntity::fileToQueryEntity();

  // Build functions
  log_info("Building the Entity clusters...");
  initEntityClusters();

}

void PostSubmissionER::initEntityClusters() {
  // Cluster entities if
  // 1 -- they have matching prefixes of size > K
  // 2 -- they have matching sufixes of size > K
  // 3 -- matching sufizes of size > K after the last ','

  size_t K = 4;

  for (int i = 0; i < entities.size(); ++i) {
    for (int j = i+1; j < entities.size(); ++j) {
      // Both string must be greater than K
      if (entities[i].name().size() < K && entities[j].name().size() < K) continue;

      if (entities[i].name().substr(0, K) == entities[j].name().substr(0, K)) {
        // Add to matrix 
        adjmap.insert(std::pair<size_t, size_t>(i, j));
        adjmap.insert(std::pair<size_t, size_t>(j, i));
        log_info("Cluster Add(pref): %s -- %s", entities[i].target_id.c_str(), entities[j].target_id.c_str());
      }

      else if ( entities[i].name().substr(entities[i].name().size() - K) ==  entities[j].name().substr(entities[j].name().size() - K)) {
        // Add to matrix 
        adjmap.insert(std::pair<size_t, size_t>(i, j));
        adjmap.insert(std::pair<size_t, size_t>(j, i));
        log_info("Cluster Add(suff): %s -- %s", entities[i].target_id.c_str(), entities[j].target_id.c_str());
      }
      // TODO check for the thrid case

    }
  }
}

std::vector<size_t> PostSubmissionER::clusterItems(QueryEntity *qe) {

  // Find the index of this query item in out entities list
  size_t index;
  for (size_t index = 0; index < entities.size(); ++index) {
    if (qe->target_id == entities[index].target_id) break;
  }

  // If it didn't find it return a blank list
  std::vector<size_t> v;
  if (index == entities.size()) return v;

  // Find all the matching list and put them in a list
  for (auto e: adjmap) {
    if (e.first == index) v.push_back(e.second);
  }
  return v;
}


void PostSubmissionER::processSubmissionFile(std::string file_name) {

  Word2Vec w;
  strcpy(w.save_vocab_file,  "/home/cgrant/projects/word2vec-read-only/vectors.vocab");
  strcpy(w.train_file,  "/home/cgrant/projects/word2vec-read-only/text8");
  strcpy(w.output_file,  "/home/cgrant/projects/word2vec-read-only/vectors.bin");

  std::ifstream infile(file_name);

  std::string line;
  while (std::getline(infile, line)) {
    if (line[0] == '#') continue;

    // Structure the line
    ssf_row row = Line2Row(line);

    // Get the MentionChain derived from the row
    MentionChain mc = ExtractMentionChain(&row);

    // Get the word clustes from the mention chain
    auto mc_words = w.distance(&mc, 50);

    std::vector<std::pair<std::string, float> > tmp(50); 
    std::vector<std::pair<std::string, float> >::iterator tmp_it;

    size_t the_max_size = -1;
    size_t the_max_index = -1;

    // Get the list of possible matching Entities
    std::vector<size_t> candidates = clusterItems(&mc.qe);
    for ( auto idx : candidates) {
      auto qe_words = w.distance(&entities[idx], 50);

      // Count the overlap between the words
      tmp_it = std::set_intersection (mc_words.begin(), mc_words.end(),
        qe_words.begin(), qe_words.end(), tmp.begin(), 
        [] (const std::pair<std::string, float> &a1, const std::pair<std::string, float> &a2 )
        -> bool {
          return a1.first == a2.first;;
        }
      );
      size_t qbc_size = tmp_it-tmp.begin();

      if ( the_max_size < qbc_size) {
        the_max_size = qbc_size;
        the_max_index = idx;
      }
    }

    size_t mc_index;
    for (size_t index = 0; index < entities.size(); ++index) {
      if (mc.qe.target_id == entities[index].target_id) break;
    }

    if (the_max_index != -1 && the_max_index != mc_index) {
      // TODO do something about the new item
      log_info("#New Element here!!");
      // Update the entity
      row.print();
    }
    else {
      row.print(); // Print the row to standard out 
    }



  }
}


ssf_row PostSubmissionER::Line2Row(std::string line) {

  ssf_row r;

  std::stringstream ss(line);

  ss >> r.team_id >> r.system_id >> r.stream_id >> r.entity_id;
  ss >> r.confidence >> r.relevance >> r.contains >> r.date_hour;
  ss >> r.slot_name >> r.slot_value >> r.byte_range;

  return r;
}


QueryEntity PostSubmissionER::ExtractQueryEntity(ssf_row *row) {

  return QueryEntity::UrlToQueryEntity(row->entity_id);
}

std::string PostSubmissionER::ExtractGPGFile(ssf_row *row) {
  size_t pos = row->stream_id.find('-')+1;
  return row->stream_id.substr(pos) + ".gpg";
}


MentionChain PostSubmissionER::ExtractMentionChain(ssf_row *row) {

  // Construct file string
  char media_sdd[150];
  char media_sde[150];
  sprintf(media_sdd, MentionChain::media_sdd, row->date_hour.c_str(), PostSubmissionER::ExtractGPGFile(row).c_str());
  sprintf(media_sde, MentionChain::media_sde, row->date_hour.c_str(), PostSubmissionER::ExtractGPGFile(row).c_str());
  
  // Check for the file in sdd and sde
  std::vector<streamcorpus::StreamItem> sis;
  if (fexist(media_sdd)) {
    log_info("media_sdd: %s", media_sdd);
    exit(1);
    sis = MentionChain::FileToStreamItem(media_sdd);
    log_info("media_sdd, sis.size() %ld", sis.size());
  }
  else {
    log_info("media_sde: %s", media_sde);
    exit(2);
    sis = MentionChain::FileToStreamItem(media_sde);
    log_info("media_sde, sis.size() %ld", sis.size());
  }

  // Fetch the StreamItem 
  streamcorpus::StreamItem si;
  for (auto s : sis ) {
    if (s.stream_id == row->stream_id) {
      si = s;
      break;
    }
  }

  // Extract the query entity
  QueryEntity qe = PostSubmissionER::ExtractQueryEntity(row);

  return MentionChain(si, qe);
}



// Take in a submission file and a the entity json file
// Output possible incorrect entities from the submission file
int main( int argc, char **argv) {

  //std::string test_line = "gatordsr gatordsr_new 1327982549-85fe459503535923e8b7d6ab8e96877f http://en.wikipedia.org/wiki/Satoshi_Ishii 1000 2 1 2012-01-31-04 Affiliate 7 525-567";

  //PostSubmissionER::Line2Row(test_line);
  
  PostSubmissionER p;
  p.processSubmissionFile("/media/sde/submission/allFullRuns/submission.txt");

  return 0;
}


