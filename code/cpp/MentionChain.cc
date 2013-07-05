

#include "MentionChain.h"
#include "streamcorpus_types.h"
#include "streamcorpus_constants.h"

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TFileTransport.h>

#include <boost/algorithm/string.hpp>

#include <algorithm>
#include <cstring>
#include <iostream>
#include <sstream>
#include <unordered_set>

std::vector<streamcorpus::StreamItem> MentionChain::FileToStreamItem(std::string filePath) {

  std::vector<streamcorpus::StreamItem> si;
  
  // Turn the file path to a file descriptor
  boost::shared_ptr<apache::thrift::transport::TTransport> transportInput(new apache::thrift::transport::TFileTransport(filePath.c_str()));
  boost::shared_ptr<apache::thrift::protocol::TBinaryProtocol> protocolInput(new apache::thrift::protocol::TBinaryProtocol(transportInput));
  transportInput->open();
  
  while (true) {
    try {
      streamcorpus::StreamItem stream_item;
      stream_item.read(protocolInput.get()); 
      si.push_back(stream_item);
    }
    catch (apache::thrift::transport::TTransportException e) {
      break;
    }
  }

  return si;
}

std::vector<MentionChain> MentionChain::ReadLine(std::string line) {
  /* An example line:
    ling>2011-11-08-23 | social-265-c321d098ea52fed0c9612e7934034dbd-bf1d637ef0f126f139abb3fceb2ecb9c.sc.xz.gpg | 239 | 60adb14343a4fafabf0e76bd435cdaec || http://en.wikipedia.org/wiki/William_H._Miller_(writer),
    Sentence>adventism william miller
  */

  char *p = strtok((char *)line.c_str(), ">");

  // Parse out the data
  p = strtok(NULL, "|"); 
  std::string date(p);
  boost::algorithm::trim(date);
  p = strtok(NULL, "|"); 

  // parse out the file name
  std::string fileName(p);
  p = strtok(NULL, "|"); 

  // Grab the streamitem index
  std::string si_index_str(p);
  boost::algorithm::trim(si_index_str);
  size_t si_index;
  std::istringstream (si_index_str) >> si_index;
  p = strtok(NULL, "|"); 

  // Doc id
  std::string docid(p);
  boost::algorithm::trim(docid);
  p = strtok(NULL, "|,"); 
  // Grab entities
  std::unordered_set<std::string> entities;
  while (p) {
    std::string en(p);
    boost::algorithm::trim(en);
    entities.insert(en); 
    p = strtok(NULL, ","); 
  }

  // Initialize Mention chain
    // Check the location of the file (sdd or sde).
    char gpgFile[256];
    snprintf(gpgFile, 256, media_sdd, date.c_str(), fileName.c_str());
    if (!fexist(gpgFile)) {
      // Try looking in the media_sde file
      snprintf(gpgFile, 256, media_sde, date.c_str(), fileName.c_str());
      assert(fexist(gpgFile));
    }
    std::cerr << "The file: " << gpgFile << "\n";
    
    // Then grab the mention items (using the method).
    auto sis = FileToStreamItem(gpgFile);
    
    // Extract the appropriate one
    streamcorpus::StreamItem si = sis[si_index];

  // Call init() method to process Mention chain. -- This should get all mentions from the doc.

  std::vector<MentionChain> mchains;
  std::for_each(entities.begin(), entities.end(), [=,&mchains] (std::string e) {
    MentionChain m(sis[si_index], e);
    m.init();
    mchains.push_back(m);
  });
  return mchains;
}


int main (int argc, char **argv) {
    std::string line("ling>2011-11-08-23 | social-265-c321d098ea52fed0c9612e7934034dbd-bf1d637ef0f126f139abb3fceb2ecb9c.sc.xz.gpg | 239 | 60adb14343a4fafabf0e76bd435cdaec || http://en.wikipedia.org/wiki/William_H._Miller_(writer), http://en.wikipedia.org/wiki/William_H._Miller");

  MentionChain::ReadLine(line);
  return 0;
}
