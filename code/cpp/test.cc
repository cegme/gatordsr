
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
    std::tmpnam(tmpfilenam);
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
      auto c = MentionChain::ReadLine(line, all_entities);
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
    "-train", "/media/sdc/wiki/enwiki-20120104-pages-articles.txt", 
    //"-train", "/home/cgrant/projects/word2vec-read-only/text8", 
    "-output", "/media/sdc/enwiki-20120104-pages-articles.bin", 
    //"-output", "/home/cgrant/projects/word2vec-read-only/vectors.bin", 
    "-cbow", "0", 
    "-size", "1000", // Too big here is dangerous
    "-window", "15", 
    "-hs", "1",
    "-negative", "0",
    "-sample", "1e6",  // Think about removing stopwords
    "-threads", "32",
    "-binary", "1",
    "-save-vocab", "/media/sdc/enwiki-20120104-pages-articles.vocab",
    "-debug", "2"};
  w.Main(25, (char **)argv);

  return false;
}


int main(int argc, char **argv) {

  //log_info("test1(): %s", (test1()) ? "true" : "false") ;
  //log_info("test2(): %s", (test2()) ? "true" : "false") ;
  //log_info("test3(): %s", (test3()) ? "true" : "false") ;
  log_info("test4(): %s", (test4()) ? "true" : "false");

  return 0;
}
