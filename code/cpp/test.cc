
#include <algorithm>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>

#include "MentionChain.h"
#include "Parameters.h"
#include "QueryEntity.h"
#include "Util.h"
#include "Word2Vec.h"

bool test1() {
  // Test the output of downloading a wikipage using the wikipedia api
  const char * page_title = "Stuart_Powell_Field";

  // Get the page
  std::string wiki_page = QueryEntity::getWikiBody(page_title);

  // Expected output (8/7/2013)
  std::string latest_wikipage ("Stuart Powell Field (ICAO: KDVK, FAA LID: DVK) is a public-use airport located 3 nautical miles (5.6 km; 3.5 mi) south of the central business district of Danville, a city in Boyle County, Kentucky, United States. It is owned by the City of Danville and Boyle County.\nAlthough most U.S. airports use the same three-letter location identifier for the FAA and IATA, this airport is assigned DVK by the FAA but has no designation from the IATA.\n\n\nFacilities and aircraft\nStuart Powell Field covers an area of 170 acres (69 ha) at an elevation of 1,022 feet (312 m) above mean sea level. It has two asphalt paved runways: 12/30 is 5,000 feet (1,524 m) by 75 feet (23 m) and 1/19 is 1,971 feet (601 m) by 75 feet (23 m).\nFor the 12-month period ending June 30, 2011, the airport had 20,450 aircraft operations, an average of 56 per day: 81% general aviation, 16% air taxi and 2% military. As of December 15, 2011, 31 aircraft were based at this airport: 26 single-engine, 4 multi-engine, 1 jet, and 1 helicopter.\n\n\nReferences\n\n\nExternal links\nFAA Terminal Procedures for DVK, effective July 25, 2013\nResources for this airport:\nFAA airport information for DVK\nAirNav airport information for KDVK\nFlightAware airport information and live flight tracker\nNOAA/NWS latest weather observations\nSkyVector aeronautical chart, Terminal Procedures");
  

  return wiki_page == latest_wikipage;
}


bool test2 () {

  std::string line("ling>2012-02-09-23 | social-280-4fe8289bc0edd8c387946aaf99c1f313-ae60d37374ebbc580152d92af9445c40.sc.xz.gpg | 0 | 0663e439c0a058dd07cf2f1573eda9b8 || http://en.wikipedia.org/wiki/Benjamin_Bronfman, http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., ");
  auto c = MentionChain::ReadLine(line);

  return false;
}


bool test3 () {

  // Create a query entity of X
  auto q = QueryEntity::targetidToQueryEntity("http://en.wikipedia.org/wiki/Benjamin_Bronfman");
  log_trace("Creating the Query entities.");
  auto all_entities = QueryEntity::fileToQueryEntity(); // Expensive!!!

  // Read some mention chains
    // create temp file
    char tmpfilenam[128];
    char cmd[512];
    mkstemp(tmpfilenam);
    // run a command to write to that  flile

    // NOTE: I have to cd to /media/sde because it is a network fs
    //std::snprintf(cmd, 512, "(cd /media/sde/runs && grep 'http://en.wikipedia.org/wiki/' run*Log.txt | sed 's/\\(^.*:\\)\\(.*|.*$\\)/\\2/' > %s )", tmpfilenam);
    std::snprintf(cmd, 512, "(cd /media/sde/runs && grep 'http://en.wikipedia.org/wiki/Benjamin_Bronfman' run*Log.txt | head | sed 's/\\(^.*:\\)\\(.*|.*$\\)/\\2/' > %s )", tmpfilenam);
    log_debug ("%s", cmd);
    system(cmd);

    std::vector<MentionChain> chains;

    std::ifstream infile(tmpfilenam);
    //infile.open(tmpfilenam);
    std::string line;
    while (std::getline(infile, line) ) {
      log_debug("line: %s", line.c_str());
      auto c = MentionChain::ReadLine(line);
      log_debug("MentionChain c.count(): %ld", c.size());
      chains.insert(chains.end(), c.begin(), c.end());
    }
    infile.close();

  // Check to see if they as seperable
  log_debug("chains.size(): %ld", chains.size());
  for (auto &mc : chains) {
    double qm_score = 0.0;
    for ( auto  p : Parameters::qm_functions) {
      qm_score += Parameters::qm_params.at(p.first) * p.second(q, mc);
    }
    log_info("mchain: [%f]  %s", qm_score, to_string(mc.tokens(), ",").c_str());
  }


  return false;
}

/** Train on the big wikipedia corpus */
bool test4 () {

  Word2Vec w;
  const char *argv[] = {"./word2vec",
    //"-train", "/media/sdc/wiki/enwiki-20120104-pages-articles.txt", 
    "-train", "/home/cgrant/projects/word2vec-read-only/text8", 
    //"-output", "/media/sdc/enwiki-20120104-pages-articles.bin", 
    //"-output", "/home/cgrant/projects/word2vec-read-only/vectors.bin", 
    "-output", "/home/cgrant/projects/word2vec-read-only/vectors.bin.1", 
    "-cbow", "0", 
    "-size", "1000", // Too big here is dangerous
    "-window", "15", 
    "-hs", "1",
    "-negative", "0",
    "-sample", "1e6",  // Think about removing stopwords
    "-threads", "16",
    "-binary", "1",
    //"-save-vocab", "/media/sdc/enwiki-20120104-pages-articles.vocab",
    "-save-vocab", "/home/cgrant/projects/word2vec-read-only/vectors.bin.1",
    "-debug", "2"};
  w.Main(25, (char **)argv);

  return false;
}


bool test5 () {

  Word2Vec w;
  strcpy(w.save_vocab_file,  "/home/cgrant/projects/word2vec-read-only/vectors.vocab");
  strcpy(w.train_file,  "/home/cgrant/projects/word2vec-read-only/text8");
  strcpy(w.output_file,  "/home/cgrant/projects/word2vec-read-only/vectors.bin");
  w.TrainModel();
  
  //const char *projection_file = "/home/cgrant/projects/word2vec-read-only/vectors.bin"; // Remove this dependency, just use the read_vocab_file
  //return w.LoadProjectionFile((char *)projection_file, true);
  return true;
}


bool test6 () {

  Word2Vec w;
  strcpy(w.save_vocab_file,  "/home/cgrant/projects/word2vec-read-only/vectors.vocab");
  strcpy(w.train_file,  "/home/cgrant/projects/word2vec-read-only/text8");
  strcpy(w.output_file,  "/home/cgrant/projects/word2vec-read-only/vectors.bin");

  log_info("LearnVocabFromTrainFile...\n");
  w.LearnVocabFromTrainFile();
  
  log_info("InitNet...\n");
  w.InitNet();

  w.PairwiseSimilarity((char*)"daisy", (char*)"daisy");
  w.PairwiseSimilarity((char*)"daisy", (char*)"donald");
  w.PairwiseSimilarity((char*)"daisy", (char*)"flower");
  w.PairwiseSimilarity((char*)"flower", (char*)"daisy");
  w.PairwiseSimilarity((char*)"hillary", (char*)"clinton");
  w.PairwiseSimilarity((char*)"clinton", (char*)"hillary");
  w.PairwiseSimilarity((char*)"benjamin", (char*)"bronfman");
  w.PairwiseSimilarity((char*)"bronfman", (char*)"benjamin");
  w.PairwiseSimilarity((char*)"saturn", (char*)"worm");
  w.PairwiseSimilarity((char*)"worm", (char*)"saturn");
  w.PairwiseSimilarity((char*)"car", (char*)"brakes");
  w.PairwiseSimilarity((char*)"brakes", (char*)"car");

  return true;
}

bool test7 () {

  Word2Vec w;
  strcpy(w.save_vocab_file,  "/home/cgrant/projects/word2vec-read-only/vectors.vocab");
  strcpy(w.train_file,  "/home/cgrant/projects/word2vec-read-only/text8");
  strcpy(w.output_file,  "/home/cgrant/projects/word2vec-read-only/vectors.bin");

  w.distance((char*)"osceola middle school", 10);
  w.distance((char*)"basic element music group band", 10);
  w.distance((char*)"basic element company", 500);


  return true;
}

bool test8 () {
  
  Word2Vec w;
  strcpy(w.save_vocab_file,  "/home/cgrant/projects/word2vec-read-only/vectors.vocab");
  strcpy(w.train_file,  "/home/cgrant/projects/word2vec-read-only/text8");
  strcpy(w.output_file,  "/home/cgrant/projects/word2vec-read-only/vectors.bin");

  // Create a query entity for basic element company
  auto qe_basic_company = QueryEntity::UrlToQueryEntity("http://en.wikipedia.org/wiki/Benjamin_Bronfman");

  // Create a query entity for basic element music group
  auto qe_basic_music = QueryEntity::UrlToQueryEntity("http://en.wikipedia.org/wiki/Buddy_MacKay");

  // link
  //auto line = "iling>2011-12-16-20 | news-290-b96bca9be7232f00fafd624f49918444-0808a4af5c89678ab64807c02a6f0940.sc.xz.gpg | 219 | 47682de607d93b9cba6eeb51dda97532 || http://en.wikipedia.org/wiki/Basic_Element_(company), http://en.wikipedia.org/wiki/Basic_Element_(company),";
  //auto line = "news-271-e33be01ccac7966a8a6c9174e6c50e67-640a501e4ba36e3cef3b4ace3be3490a.sc.xz.gpg | 20 | 507fab9bb5ff007c49ba7b62691d9925 || http://en.wikipedia.org/wiki/Benjamin_Bronfman, http://en.wikipedia.org/wiki/Edgar_Bronfman,_Jr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Jr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr.,";
  auto line = "ling>2012-02-12-13 | news-190-b71481cd988df153cf879da835874309-7538cf396db94d0452000a67260c4fbd.sc.xz.gpg | 144 | bef614638845b038fd0580091cf9aa20 || http://en.wikipedia.org/wiki/Buddy_MacKay, http://en.wikipedia.org/wiki/Buddy_MacKay,";

  auto mc = MentionChain::ReadLine(line);

  auto mc_t = to_string(mc[0].tokens(), " ");
  log_info("mc_t.size(): %ld", mc_t.size());
  std::transform(mc_t.begin(), mc_t.end(), mc_t.begin(), tolower);
  auto mc_words = w.distance((char*) mc_t.c_str(), 50);
  std::set<std::string> mc_set;
  log_info("mc_t: %s", mc_t.c_str());
  for(auto a : mc_words) mc_set.insert(a.first);

  auto qbc_e = to_string(qe_basic_company.aliases, " ");
  log_info("qbc_e.size(): %ld", qbc_e.size());
  std::transform(qbc_e.begin(), qbc_e.end(), qbc_e.begin(), tolower);
  auto qbc_words = w.distance((char*)qbc_e.c_str(), 50);
  std::set<std::string> qbc_set;
  log_info("qbc_e: %s", qbc_e.c_str());
  for(auto a : qbc_words) qbc_set.insert(a.first);

  std::vector<std::string> tmp(50); 
  std::vector<std::string>::iterator tmp_it;
  tmp_it = std::set_intersection (mc_set.begin(), mc_set.end(), qbc_set.begin(), qbc_set.end(), tmp.begin());
  size_t qbc_size = tmp_it-tmp.begin();  
  
  auto qbm_e = to_string(qe_basic_music.aliases, " ");
  log_info("qbm_e.size(): %ld", qbm_e.size());
  std::transform(qbm_e.begin(), qbm_e.end(), qbm_e.begin(), tolower);
  auto qbm_words = w.distance((char*)qbm_e.c_str(), 50);
  std::set<std::string> qbm_set;
  log_info("qbm_e: %s", qbm_e.c_str());
  for(auto a : qbm_words) qbm_set.insert(a.first);

  tmp_it = std::set_intersection (mc_set.begin(), mc_set.end(), qbm_set.begin(), qbm_set.end(), tmp.begin());
  size_t qbm_size = tmp_it-tmp.begin();  
  
  log_info("mc_t: %s", mc_t.c_str());
  log_info("qbm_size: %ld", qbm_size);
  log_info("qbm_e: %s", qbm_e.c_str());
  log_info("qbc_size: %ld", qbc_size);
  log_info("qbc_e: %s", qbc_e.c_str());
  
  return true;
}

bool test9 () {
  
  Word2Vec w;
  strcpy(w.save_vocab_file,  "/home/cgrant/projects/word2vec-read-only/vectors.vocab");
  strcpy(w.train_file,  "/home/cgrant/projects/word2vec-read-only/text8");
  strcpy(w.output_file,  "/home/cgrant/projects/word2vec-read-only/vectors.bin");

  // Create a query entity for basic element company
  auto qe_basic_company = QueryEntity::UrlToQueryEntity("http://en.wikipedia.org/wiki/Clare_Bronfman");

  // Create a query entity for basic element music group
  auto qe_basic_music = QueryEntity::UrlToQueryEntity("http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr.");

  // link
  auto line = "ling>2011-10-17-16 | social-308-b4f34811784fa987f4351499baddce4e-0d6f1249a0f75b4709f7c747a05e2632.sc.xz.gpg | 131 | d16e5ed1eadd235e8998c29de827ed16 || http://en.wikipedia.org/wiki/Clare_Bronfman, http://en.wikipedia.org/wiki/Clare_Bronfman, http://en.wikipedia.org/wiki/Clare_Bronfman, http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr., http://en.wikipedia.org/wiki/Edgar_Bronfman,_Sr.,";

  auto mc = MentionChain::ReadLine(line);

  auto mc_t = to_string(mc[0].tokens(), " ");
  log_info("mc_t.size(): %ld", mc_t.size());
  std::transform(mc_t.begin(), mc_t.end(), mc_t.begin(), tolower);
  auto mc_words = w.distance((char*) mc_t.c_str(), 50);
  std::set<std::string> mc_set;
  log_info("mc_t: %s", mc_t.c_str());
  for(auto a : mc_words) mc_set.insert(a.first);

  auto qbc_e = to_string(qe_basic_company.aliases, " ");
  log_info("qbc_e.size(): %ld", qbc_e.size());
  std::transform(qbc_e.begin(), qbc_e.end(), qbc_e.begin(), tolower);
  auto qbc_words = w.distance((char*)qbc_e.c_str(), 50);
  std::set<std::string> qbc_set;
  log_info("qbc_e: %s", qbc_e.c_str());
  for(auto a : qbc_words) qbc_set.insert(a.first);

  std::vector<std::string> tmp(50); 
  std::vector<std::string>::iterator tmp_it;
  tmp_it = std::set_intersection (mc_set.begin(), mc_set.end(), qbc_set.begin(), qbc_set.end(), tmp.begin());
  size_t qbc_size = tmp_it-tmp.begin();  
  
  auto qbm_e = to_string(qe_basic_music.aliases, " ");
  log_info("qbm_e.size(): %ld", qbm_e.size());
  std::transform(qbm_e.begin(), qbm_e.end(), qbm_e.begin(), tolower);
  auto qbm_words = w.distance((char*)qbm_e.c_str(), 50);
  std::set<std::string> qbm_set;
  log_info("qbm_e: %s", qbm_e.c_str());
  for(auto a : qbm_words) qbm_set.insert(a.first);

  tmp_it = std::set_intersection (mc_set.begin(), mc_set.end(), qbm_set.begin(), qbm_set.end(), tmp.begin());
  size_t qbm_size = tmp_it-tmp.begin();  
  
  log_info("mc_t: %s", mc_t.c_str());
  log_info("qbm_size: %ld", qbm_size);
  log_info("qbm_e: %s", qbm_e.c_str());
  log_info("qbc_size: %ld", qbc_size);
  log_info("qbc_e: %s", qbc_e.c_str());
  
  return true;
}




int main(int argc, char **argv) {

  //log_info("test1(): %s", (test1()) ? "true" : "false") ;
  //log_info("test2(): %s", (test2()) ? "true" : "false") ;
  //log_info("test3(): %s", (test3()) ? "true" : "false") ;
  //log_info("test4(): %s", (test4()) ? "true" : "false");
  //log_info("test5(): %s", (test5()) ? "true" : "false");
  //log_info("test6(): %s", (test6()) ? "true" : "false");
  //log_info("test7(): %s", (test7()) ? "true" : "false");
  //log_info("test8(): %s", (test8()) ? "true" : "false");
  log_info("test9(): %s", (test9()) ? "true" : "false");

  return 0;
}
