

#include "MentionChain.h"
#include "Parameters.h"
#include "QueryEntity.h"
#include "streamcorpus_types.h"
#include "streamcorpus_constants.h"
#include "Util.h"

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TFDTransport.h>
#include <thrift/transport/TFileTransport.h>
#include <thrift/transport/TBufferTransports.h>

#ifdef HAVE_NETINET_IN_H
#include <netinet/in.h>
#endif

#include <boost/algorithm/string.hpp>

#include <fcntl.h>

#include <algorithm>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <sstream>
#include <queue>


std::string MentionChain::printSentence(streamcorpus::Sentence s) {
  std::stringstream ss;
  for (auto &token : s.tokens) {
    ss << token.token << " ";
  }
  //std::cerr << ss.str() << "\n";
  return ss.str();
}

double MentionChain::TokenScore(QueryEntity qe, streamcorpus::Token t) {

  // TODO 1- make scores for certain properties, 
  // TODO 2- Sum up the scores of the properties

  // TODO 3- In the calling function if the two tokens are adjacent we combine the scores by entity_id
  // TODO 4- We keep the highest scoring equiv_id.
  // TODO 5- Find the highest scoring equiv_id and put them in a (sentence_num, sentence_pos) map.
  double qt_score = 0.0;
  for (auto &func: Parameters::qt_functions) {
    qt_score +=  Parameters::qt_params.at(func.first) * func.second(qe, t);
    //log_info("~~~~~~~~ %d",  func.second(qe, t));
  }

  //if (qt_score > 0.0) log_info("qt_score %f :: [%s] :: %s", qt_score,  t.token.c_str(), qe.toString().c_str()); 
  return qt_score;
}


struct equiv_struct {
  int32_t equiv_id;
  double score;

  // Sentence token numbers locations
  // <sentence_num, sentence_pos> 
  std::vector<std::pair<size_t,size_t> > loc;

  equiv_struct add (double _score, size_t sentence_num, size_t sentence_pos) {
    score += _score;
    loc.push_back(std::make_pair(sentence_num, sentence_pos));
    return *this;
  }

  bool operator()(const equiv_struct& a, const equiv_struct& b) const {
    return a.score < b.score;
  }
};

void MentionChain::init() {

  // Find an entity id that is associated with the entity string
  // Go through all the sentences
  // Pick out all the possible entity Ids that may be associated with this mention
  // Keep a list of (sentencenum, tokennums)
  
  // -------

  // Create a vector of equiv_ids
  std::map<int32_t, equiv_struct> equiv_map;

  // Find the winningest equiv_id
  size_t sentence_num = 0;
  for (auto &sentence : si.body.sentences.at("lingpipe")) {

    //std::cerr << "-------------------\n";
    //std::cerr << sentence_num << ": " << printSentence(sentence) << "\n";
    //std::cerr << "-------------------\n";
    for (auto &token : sentence.tokens) {
      if (token.equiv_id == -1) continue;
  
      // Create a map of entity ids to tokens
      // Create a function that turns the map into a (sentence, tokennum) map
      // Score each of the chains to find the besst one.

      //std::cerr << "token: " << token.token << "\n";
      //std::cerr << "\ttoken_num: " << token.token_num << "\n";
      //std::cerr << "\tsentence_pos: " << token.sentence_pos << "\n";
      //std::cerr << "\tmention_id: " << token.mention_id << "\n";
      //std::cerr << "\tlemma: " << token.lemma << "\n";
      //std::cerr << "\tpos: " << token.pos << "\n";
      //std::cerr << "\tequiv_id: " << token.equiv_id << "\n";
      //std::cerr << "\tparent_id: " << token.parent_id << "\n";
      //std::cerr << "\tdependency_path: " << token.dependency_path << "\n";

      double total = 0.0;
      /*auto qt_params = Parameters::qt_params;
      for(auto qt: Parameters::qt_functions) {
        // Indicator funtion * feature weight
        total += qt.second(qe, token) * qt_params[qt.first];
      }*/
      total += TokenScore(qe, token);

      // Add this token to the priority_queue
      if(equiv_map.find(token.equiv_id) == equiv_map.end()) {
        struct equiv_struct e;
        e.equiv_id = token.equiv_id; 
        e.add(total, sentence_num, token.sentence_pos);
        
        equiv_map.insert(std::make_pair(token.equiv_id, e));
      }
      else {
        equiv_map[token.equiv_id].add(total, sentence_num, token.sentence_pos);
      }

    }
    ++sentence_num;
  }

  // If the size of the equiv_map is empty, 
  // Set query entity to null
  if (equiv_map.empty()) {
    QueryEntity _q;
    this->qe = _q;
  }
  else {
    // If not, find the highest score
    // Set QueryEntity to the best one
    auto maxElement = std::max_element(equiv_map.begin(), equiv_map.end(), 
      [=] (const std::pair<int32_t,equiv_struct> &a, const std::pair<int32_t,equiv_struct> &b) {
        return a.second.score < b.second.score;
    });
    this->locations = maxElement->second.loc;
    this->equiv_id = maxElement->first;

  }
}


std::string MentionChain::get(size_t idx) const {
  size_t sentence_num = this->locations[idx].first;
  size_t sentence_pos = this->locations[idx].second;

  std::map<std::string, std::vector<streamcorpus::Sentence> > ss = this->si.body.sentences;
  const std::string token = ss["lingpipe"][sentence_num].tokens[sentence_pos].token;
  return token;
}


std::vector<std::string> MentionChain::tokens() const {
  //log_info("locations.size(): %ld", locations.size());
  std::vector<std::string> t;
  for (size_t idx = 0; idx != locations.size(); ++idx) {
    size_t sentence_num = locations[idx].first;
    size_t sentence_pos = locations[idx].second;
    std::map<std::string, std::vector<streamcorpus::Sentence> > ss = si.body.sentences;
    std::string token = ss["lingpipe"][sentence_num].tokens[sentence_pos].token;
    t.push_back(token);
  }
  return t;
}
   

std::vector<streamcorpus::StreamItem> MentionChain::FileToStreamItem(std::string filePath) {

  std::vector<streamcorpus::StreamItem> sis;

  int fd = open(filePath.c_str(), O_RDONLY);
  boost::shared_ptr<apache::thrift::transport::TFDTransport> transportInput(new apache::thrift::transport::TFDTransport(fd, apache::thrift::transport::TFDTransport::ClosePolicy::CLOSE_ON_DESTROY));
  //boost::shared_ptr<apache::thrift::transport::TFileTransport> transportInput(new apache::thrift::transport::TFileTransport(filePath, true));
  boost::shared_ptr<apache::thrift::transport::TBufferedTransport> buffTransportInput(new apache::thrift::transport::TBufferedTransport(transportInput));
  //boost::shared_ptr<apache::thrift::protocol::TBinaryProtocol> protocolInput(new apache::thrift::protocol::TBinaryProtocol(buffTransportInput));
  boost::shared_ptr<apache::thrift::protocol::TBinaryProtocol> protocolInput(new apache::thrift::protocol::TBinaryProtocol(transportInput));

    //buffTransportInput->open();
    //transportInput->open();

    //std::cerr << "isOpen: " << transportInput->isOpen()?"true":"false";
    //std::cerr << "\n";

    while (true) {
      try {

        streamcorpus::StreamItem stream_item;
        stream_item.read(protocolInput.get()); 

        //std::cerr << "||" << stream_item.body.clean_visible << "\n";

        sis.push_back(stream_item);
        //std::cerr << "sis.size(): " << sis.size() << "\n";
      }
      catch (apache::thrift::transport::TTransportException &e) {
        bool eof = false;
        switch(e.getType()) {
          case apache::thrift::transport::TTransportException::END_OF_FILE:
            //std::cerr <<"TTransportException: End of file\n";
            eof = true;
            break;
          case apache::thrift::transport::TTransportException::UNKNOWN: std::cerr <<"TTransportException: Unknown transport exception\n"; break;
          case apache::thrift::transport::TTransportException::NOT_OPEN: std::cerr <<"TTransportException: Transport not open\n"; break;
          case apache::thrift::transport::TTransportException::TIMED_OUT: std::cerr <<"TTransportException: Timed out\n"; break;
          case apache::thrift::transport::TTransportException::INTERRUPTED: std::cerr <<"TTransportException: Interrupted\n"; break;
          case apache::thrift::transport::TTransportException::BAD_ARGS: std::cerr <<"TTransportException: Invalid arguments\n"; break;
          case apache::thrift::transport::TTransportException::CORRUPTED_DATA: std::cerr <<"TTransportException: Corrupted Data\n"; break;
          case apache::thrift::transport::TTransportException::INTERNAL_ERROR: std::cerr << "TTransportException: Internal error\n"; break;
          default: std::cerr << "TTransportException: (Invalid exception type)\n"; break;
        }
        //std::cerr << "Stack err: " << e.what() << "\n";
        if(eof) break;
      }
      catch (apache::thrift::protocol::TProtocolException &e) {
        std::cerr << "Protocol has a negative size\n";
        break;
      }
    }

  return sis;
}

std::vector<MentionChain> MentionChain::ReadLine(std::string line) {
  /* An example line:
    ling>2011-11-08-23 | social-265-c321d098ea52fed0c9612e7934034dbd-bf1d637ef0f126f139abb3fceb2ecb9c.sc.xz.gpg | 239 | 60adb14343a4fafabf0e76bd435cdaec || http://en.wikipedia.org/wiki/William_H._Miller_(writer),
    Sentence>adventism william miller
  */

  char *data_line = new char[line.size()+1];
  std::strcpy (data_line, line.c_str());
  char *p = std::strtok(data_line, ">");

  // Parse out the data
  p = std::strtok(p, "|");  // Need to update the tokenizer with the new string
  std::string date(p);
  boost::algorithm::trim(date);
  p = std::strtok(NULL, "|"); 

  // parse out the file name
  std::string fileName(p);
  boost::algorithm::trim(fileName);
  p = std::strtok(NULL, "|"); 

  // Grab the streamitem index
  std::string si_index_str(p);
  boost::algorithm::trim(si_index_str);
  size_t si_index;
  std::istringstream (si_index_str) >> si_index;
  p = std::strtok(NULL, "|"); 

  // Doc id
  std::string docid(p);
  boost::algorithm::trim(docid);
  p = std::strtok(NULL, "|,"); 

  //Get the List of Query Entities
  std::vector<QueryEntity> all_entities = QueryEntity::fileToQueryEntity();

  // Grab entities
  std::list<QueryEntity> entities;
  while (p) {
    std::string en(p);
    boost::algorithm::trim(en);
    if (!en.empty()) { // Take care of the trailing comma
      entities.push_back( QueryEntity::targetidToQueryEntity(en, all_entities) ); 
    }
    p = std::strtok(NULL, ","); 
  }

  // Delete created string
  delete [] data_line;
  

  // Initialize Mention chain
    // Check the location of the file (sdd or sde).
    char gpgFile[500];
    snprintf(gpgFile, 500, media_sdd, date.c_str(), fileName.c_str());
    //std::cerr << "gpgFile_sdd: " << gpgFile << "\n";
    if (!fexist(gpgFile)) {
      // Try looking in the media_sde file
      snprintf(gpgFile, 500, media_sde, date.c_str(), fileName.c_str());
      //std::cerr << "gpgFile_sde: " << gpgFile << "\n";
      assert(fexist(gpgFile));
    }

    // decrypt and decompress the file to a local directory
    std::string tmpGpg (CreateTempGPGFile(gpgFile));
    
    // Then grab the mention items (using the method).
    auto sis = FileToStreamItem(tmpGpg);

    // Remove that tmp file
    remove(tmpGpg.c_str());
    
    // Extract the appropriate one
    //std::cerr << "si_index: " << si_index << "\n";
    streamcorpus::StreamItem si(sis[si_index]);
    
  std::vector<MentionChain> mchains;
  std::for_each(entities.begin(), entities.end(), [=,&mchains] (QueryEntity qe) {
    MentionChain m(si, qe);
    m.init(); // TODO do we need to process all the Mention Chains
    mchains.push_back(m);
  });
  //log_info ( "mchains.size() %ld", mchains.size() );
  return mchains;
}


std::string MentionChain::CreateTempGPGFile (std::string gpgFileName) {
  char cmd[512+L_tmpnam+L_tmpnam]; // The command to decrypt 
  char fname [512+L_tmpnam]; // the tmp file name
  char fullpath [512+L_tmpnam]; // The full path of the file name 

  // Get random temp file name
  tmpnam (fname);
  //std::cerr << "fname: " << fname << "\n";

  // Make the full file path
  snprintf(fullpath, 512+L_tmpnam, "%s", fname);
  ///std::cerr << "fullpath: " << fullpath << "\n";
  snprintf(cmd, 512+L_tmpnam+L_tmpnam, gpgDecompress, gpgFileName.c_str(), fullpath);
  //std::cerr << "cmd: " << cmd << "\n";

  // Use system(" " ) to decrypt and decompress file here
  system(cmd); // Create the file
  return std::string(fullpath); // Return the path
}


int test(int argc, char **argv) {
  std::string line("ling>2011-11-08-23 | social-265-c321d098ea52fed0c9612e7934034dbd-bf1d637ef0f126f139abb3fceb2ecb9c.sc.xz.gpg | 239 | 60adb14343a4fafabf0e76bd435cdaec || http://en.wikipedia.org/wiki/William_H._Miller_(writer), http://en.wikipedia.org/wiki/William_H._Miller");

  QueryEntity::fileToQueryEntity("../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json");

  std::vector<MentionChain> m(MentionChain::ReadLine(line));

  std::for_each(m.begin(), m.end(), [&m] (MentionChain& mc) {
    mc.init();
  });
  return 0;
}





