

#include "MentionChain.h"
#include "QueryEntity.h"
#include "streamcorpus_types.h"
#include "streamcorpus_constants.h"

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/transport/TFDTransport.h>
#include <thrift/transport/TFileTransport.h>
#include <thrift/transport/TBufferTransports.h>

#include <boost/algorithm/string.hpp>

#include <fcntl.h>
#include <algorithm>
#include <cstdio>
#include <cstring>
#include <iostream>
#include <sstream>
#include <unordered_set>


void MentionChain::init() {

  // Find an entity id that is associated with the entity string
  // Go through all the sentences
  // Pick out all the possible entity Ids that may be associated with this mention
  // Keep a list of (sentencenum, tokennums)
  

}



std::vector<streamcorpus::StreamItem> MentionChain::FileToStreamItem(std::string filePath) {

  std::vector<streamcorpus::StreamItem> sis;

  //std::cerr << "filePath: " << filePath << "\n" ;
  
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
        if(eof)
          break;
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

  char *p = strtok((char *)line.c_str(), ">");

  // Parse out the data
  p = strtok(NULL, "|"); 
  std::string date(p);
  boost::algorithm::trim(date);
  p = strtok(NULL, "|"); 

  // parse out the file name
  std::string fileName(p);
  boost::algorithm::trim(fileName);
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
    // TODO decrypt and decompress the file to a local directory
    // TODO Read the file and delete the temp file
    char gpgFile[500];
    snprintf(gpgFile, 500, media_sdd, date.c_str(), fileName.c_str());
    //std::cerr << "gpgFile_sdd: " << gpgFile << "\n";
    if (!fexist(gpgFile)) {
      // Try looking in the media_sde file
      snprintf(gpgFile, 500, media_sde, date.c_str(), fileName.c_str());
      //std::cerr << "gpgFile_sde: " << gpgFile << "\n";
      assert(fexist(gpgFile));
    }

    std::string tmpGpg = CreateTempGPGFile(gpgFile);
    //std::cerr << "tmpGpg: " << tmpGpg << "\n";
    
    // Then grab the mention items (using the method).
    auto sis = FileToStreamItem(tmpGpg);

    // Remove that tmp file
    //remove(tmpGpg.c_str());
    
    // Extract the appropriate one
    //std::cerr << "si_index: " << si_index << "\n";
    streamcorpus::StreamItem si(sis[si_index]);
    
    //std::cerr << "|\n" << si.body.clean_visible << "\n";

  std::vector<MentionChain> mchains;
  std::for_each(entities.begin(), entities.end(), [=,&mchains] (std::string e) {
    MentionChain m(si, e);
    m.init(); // Process the Mention Chains
    mchains.push_back(m);
  });
  return mchains;
}


std::string MentionChain::CreateTempGPGFile (std::string gpgFileName) {
  char cmd[500+L_tmpnam+L_tmpnam]; // The command to decrypt 
  char fname [500+L_tmpnam]; // the tmp file name
  char fullpath [500+L_tmpnam]; // The full path of the file name 

  // Get random temp file name
  tmpnam (fname);
  //std::cerr << "fname: " << fname << "\n";

  // Make the full file path
  snprintf(fullpath, 500+L_tmpnam, "%s", fname);
  ///std::cerr << "fullpath: " << fullpath << "\n";
  snprintf(cmd, 500+L_tmpnam+L_tmpnam, gpgDecompress, gpgFileName.c_str(), fullpath);
  //std::cerr << "cmd: " << cmd << "\n";

  // Use system(" " ) to decrypt and decompress file here
  system(cmd); // Create the file
  return std::string(fullpath); // Return the path
 }


int main (int argc, char **argv) {
    std::string line("ling>2011-11-08-23 | social-265-c321d098ea52fed0c9612e7934034dbd-bf1d637ef0f126f139abb3fceb2ecb9c.sc.xz.gpg | 239 | 60adb14343a4fafabf0e76bd435cdaec || http://en.wikipedia.org/wiki/William_H._Miller_(writer), http://en.wikipedia.org/wiki/William_H._Miller");

  MentionChain::ReadLine(line);

  QueryEntity::fileToQueryEntity("../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json");
  return 0;
}





