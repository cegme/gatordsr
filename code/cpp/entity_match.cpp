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

#include <thrift/protocol/TBinaryProtocol.h>
#include <thrift/protocol/TDenseProtocol.h>
#include <thrift/protocol/TJSONProtocol.h>
#include <thrift/transport/TTransportUtils.h>
#include <thrift/transport/TFDTransport.h>
#include <thrift/transport/TFileTransport.h>

#include <boost/filesystem.hpp>
#include <boost/filesystem/path.hpp>
#include <boost/program_options.hpp>

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

    //clog << "Starting program" <<endl;
    //clog << "File name: " << argv[1] << endl;
    string gpg_file;
    if (argc > 1) {
      gpg_file = argv[1];
    }
    
    locale loc;
    string text_source("clean_visible");

    bool negate(false);

    // Supported options.
    po::options_description desc("Allowed options");
    desc.add_options()
    ("help,h", "help message")
    ("gpgfile,f", "gpgfile")
    //("text_source,t", po::value<string>(&text_source), "text source in stream item")
    //("negate,n", po::value<bool>(&negate)->implicit_value(true), "negate sense of match")
    ;

    // Parse command line options
    po::variables_map vm;
    po::store(po::parse_command_line(argc, argv, desc), vm);
    po::notify(vm);

    /*if (vm.count("help")) {
        cout << desc << "\n";
        return 1;
    }*/

    // Read in the entity JSON file
    std::vector<found_entity> aliases(get_aliases());

    // Create annotator object
    Annotator annotator;
    AnnotatorID annotatorID;
    annotatorID = "example-matcher-v0.1";

    // Annotator identifier
    annotator.annotator_id = "example-matcher-v0.1";

    // Time this annotator was started
    StreamTime streamtime;
    time_t seconds;
    seconds = time(NULL);
    streamtime.epoch_ticks = seconds;
    streamtime.zulu_timestamp = ctime(&seconds);
    annotator.__set_annotation_time(streamtime);

    // Setup thrift reading and writing from stdin and stdout
    int input_fd = 0;
    int output_fd = 1;

    // input
    boost::shared_ptr<TFDTransport> innerTransportInput(new TFDTransport(input_fd));
    boost::shared_ptr<TBufferedTransport> transportInput(new TBufferedTransport(innerTransportInput));
    boost::shared_ptr<TBinaryProtocol> protocolInput(new TBinaryProtocol(transportInput));
    transportInput->open();
    //clog << "isOpen : " << transportInput->isOpen() << endl;

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
                cerr << "Bad text_source :" << text_source <<endl;
                exit(-1);
            }

            if (content.empty()) {
                // Fall back to raw if desired text_source has no content.
                content = stream_item.body.raw;
                boost::algorithm::to_lower(content);

                actual_text_source = "raw";
                if (content.empty()) {
                    // If all applicable text sources are empty, we have a problem and exit with an error
                    cerr << si_total << " Error, doc id: " << stream_item.doc_id << " was empty." << endl;
                    exit(-1);
                }

              // Check for an entity match
              
              struct HasEntity has_entity(content);

              if(streamcorpus::any_of(aliases.begin(), aliases.end(), has_entity)) {
              //if(streamcorpus::any_ofs(aliases.cbegin(), aliases.cend(), HasEntity(content))) {
              //if(std::any_of(aliases.begin(), aliases.end(), HasEntity(content))) {
                
                // Found an entity, print which one
                //std::clog << "Found an entity in stream item: " << stream_item.doc_id;
                ++si_match;
                // TODO Add a call to a function that prints out the file matched entity relations
                //
              }
            }
          


            // Increment count of stream items processed
            si_total++;
        }
        catch (TTransportException e) {
            // Vital to flush the buffered output or you will lose the last one
            if (si_match > 0) {
              //clog << "si processed: " << si_total <<  ", matches :" << si_match << endl;
              clog << "[" << si_match << "]|" << gpg_file << endl;
            }
            break;
        }
    }
    return 0;
}

