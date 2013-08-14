
#ifndef MENTION_CHAIN_H
#define MENTION_CHAIN_H

#include <fstream>
#include <map>
#include <string>

#include "streamcorpus_types.h"
#include "streamcorpus_constants.h"

#include "QueryEntity.h"


class MentionChain {

  constexpr static const char *media_sdd = "/media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/"
    "kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/%s/%s";
  constexpr static const char *media_sde = "/media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/"
    "kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/%s/%s";
  constexpr static const char *tmp_prefix = "/var/tmp";
  constexpr static const char *gpgDecompress = "gpg --quiet --no-verbose --no-permission-warning --trust-model always --output - --decrypt %s | xz --decompress > %s";

//private:
public:
  std::string day;
  std::string fileName;
  int stream_index;
  std::string docid;
  //std::string entity;
  QueryEntity qe;
  streamcorpus::StreamItem si;

  // TODO map or pointer to mentions
  // TODO - Keep a pool of docs??
  // Pairs of <sentence_num, sentence_pos>
  std::vector<std::pair<size_t,size_t> > locations;
  int32_t equiv_id;

public:
  MentionChain() {}
  MentionChain(streamcorpus::StreamItem _si, QueryEntity _qe): si(_si), qe(_qe) { }

  /**
    * TODO: This function searches for all the entity mentions in the streamitem documents.
    * TODO: This also extracts/creates a feature vector
    */
  void init ();

  std::string get(size_t idx) const;
  inline size_t tokencount () const { return locations.size(); }
  std::string clean_visible () const { return si.body.clean_visible; }

  /** Get a list of all tokens */
  std::vector<std::string> tokens() const;
    
  // Pretty print the data structure
  std::string pretty_string() const;

  /** Take a line from a runXLog.txt file and make it into a MentionChain */
  static std::vector<MentionChain> ReadLine(std::string line);
  static std::vector<MentionChain> ReadLine(std::string line, std::vector<QueryEntity>);

  /** Take a thrift file and extract all the stream items and put them in a vector */
  static std::vector<streamcorpus::StreamItem> FileToStreamItem(std::string filePath); 

  /**
    * Decompresses and dencrypts a file to a temporary file and returns the file name.
    */
  static std::string CreateTempGPGFile (std::string fileName);

  /** Utility function to print get a sentence string */
  static std::string printSentence(streamcorpus::Sentence s);

  /** Return a score that the token is associated with the query entity */
  static double TokenScore(QueryEntity qe, streamcorpus::Token t);
  
};




#endif  // MENTION_CHAIN_H

