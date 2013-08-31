#include <inttypes.h>
#include <fcntl.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/time.h>
#include <netinet/in.h>
#include <iostream>
#include <cstdio>
#include <time.h>
#include <boost/unordered_map.hpp>
#include <boost/lexical_cast.hpp>
#include <boost/algorithm/string.hpp>
#include <fstream>
#include <vector>
#include <algorithm>
#include <locale>

#include "streamcorpus_types.h"
#include "streamcorpus_constants.h"
#include "entity_match.h"
#include "Util.h"

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/protocol/TDenseProtocol.h>
#include <thrift/protocol/TJSONProtocol.h>
#include <thrift/transport/TTransportUtils.h>
#include <thrift/transport/TFDTransport.h>
#include <thrift/transport/TFileTransport.h>

#include <boost/filesystem.hpp>
#include <boost/filesystem/path.hpp>
#include <boost/program_options.hpp>

#include "alias_vector.h"

using namespace std;
using namespace boost;
using namespace boost::filesystem;
using namespace apache::thrift;
using namespace apache::thrift::protocol;
using namespace apache::thrift::transport;

namespace po = boost::program_options;

using namespace streamcorpus;




//------------------------------------

int main(int argc, char **argv) {

  string gpg_file;
  if (argc > 1) {
    gpg_file = argv[1];
  }
  //log_info("The gpgfile: %s", argv[1]);

  locale loc;
  string text_source("clean_visible");

  bool negate(false);

  // Read in the entity JSON file
  //std::vector<found_entity> aliases(get_aliases());
  std::vector<found_entity> aliases(alias_vector); // A generated map file

  // Setup thrift reading and writing from stdin and stdout
  int input_fd = 0;
  int output_fd = 1;

  // input
  boost::shared_ptr<TFDTransport> innerTransportInput(new TFDTransport(input_fd));
  boost::shared_ptr<TBufferedTransport> transportInput(new TBufferedTransport(innerTransportInput));
  boost::shared_ptr<TBinaryProtocol> protocolInput(new TBinaryProtocol(transportInput));
  transportInput->open();

  // Read and process all stream items
  StreamItem stream_item;
  int si_total=0;
  int si_match=0;

  while (true) {
    try {
      // Read stream_item from stdin
      stream_item.read(protocolInput.get());

      string content;
      string actual_text_source = text_source;
      //clog << "Reading stream item content from : " << text_source << endl;
      if (text_source == "clean_visible") {
        content = stream_item.body.clean_visible;
      } else if (text_source == "clean_html") {
        content = stream_item.body.clean_html;
      } else if (text_source == "raw") {
        content = stream_item.body.raw;
      } else {
        log_err("Bad text_source: %s", text_source.c_str());
        break;
      }

      if (content.empty()) {
        // Fall back to raw if desired text_source has no content.
        content = stream_item.body.raw;
        boost::algorithm::to_lower(content);
        actual_text_source = "raw";


        if (content.empty()) {
          // If all applicable text sources are empty, we have a problem and exit with an error
          log_err("%d Error, doc id: %s was empty", si_total, stream_item.doc_id.c_str() );
          continue;
          //exit(-1);
        }
      }

      // Check for an entity match
      struct HasEntity has_entity(content);
      //boost::algorithm::to_lower(content);
      
      if(streamcorpus::any_of(aliases.begin(), aliases.end(), has_entity)) {
      //if(streamcorpus::any_of(aliases.begin(), aliases.end(), [content] (const found_entity &a) {
            //return content.find(a.alias) != std::string::npos;
            //return boost::regex_search(content, a.alias_regex);
        //})) {

        // Found an entity, print which one
        ++si_match;
        if (false) { // This slows down the process so we will not do it
          for (auto &a : aliases) {
            if (content.find(a.alias) != std::string::npos) {
              auto pos = content.find(a.alias);
              log_info("The match is: <%s | %s>", a.alias.c_str(), content.substr(pos, a.alias.length()).c_str());
              break;
            }
          }
          while(stream_item.read(protocolInput.get())); // Read the rest of the pipe so we are not rude
        }
        break; // Just find the first
      }

      // Increment count of stream items processed
      si_total++;
    }
    catch (TTransportException e) {
      bool eof = false;
      switch(e.getType()) {
        case apache::thrift::transport::TTransportException::END_OF_FILE:
          // This is the only acceptable exception
          eof = true;
          break;
        case apache::thrift::transport::TTransportException::UNKNOWN: log_err("TTransportException: Unknown transport exception\n"); eof = true; break;
        case apache::thrift::transport::TTransportException::NOT_OPEN: log_err("TTransportException: Transport not open\n"); eof = true; ;
        case apache::thrift::transport::TTransportException::TIMED_OUT: log_err("TTransportException: Timed out\n"); eof = true; break;
        case apache::thrift::transport::TTransportException::INTERRUPTED: log_err("TTransportException: Interrupted\n"); eof = true; break;
        case apache::thrift::transport::TTransportException::BAD_ARGS: log_err("TTransportException: Invalid arguments\n"); eof = true; break;
        case apache::thrift::transport::TTransportException::CORRUPTED_DATA: log_err("TTransportException: Corrupted Data\n"); eof = true; break;
        case apache::thrift::transport::TTransportException::INTERNAL_ERROR: log_err( "TTransportException: Internal error\n"); eof = true; break;
        default: log_err( "TTransportException: (Invalid exception type)\n"); break;
      }
      //log_err("Stack err: %s [%d]", e.what(), e.getType());
      if(eof) break;
    }
  }
  //log_info("si_total: %d", si_total);
  string is_good = (si_match>0)?"+ |":"- | ";
  cout << is_good << gpg_file << "|" << si_total << endl;

  return 0;
}

