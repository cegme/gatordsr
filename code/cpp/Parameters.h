
#ifndef PARAMETERS_H
#define PARAMETERS_H

#include <functional>
#include <map>
#include <string>

#include "streamcorpus_constants.h"
#include "streamcorpus_types.h"

#include "MentionChain.h"
#include "QueryEntity.h"
#include "Entity.h"

class Parameters {
  friend class MentionChain;
  friend class QueryEntity;

private:

  /** Comparison functions between query token and mention chain */
  static bool qm_qe_match(QueryEntity qe, const MentionChain &m);
  static bool qm_jaccard_80(QueryEntity qe, const MentionChain &m);
  static bool qm_jaccard_90(QueryEntity qe, const MentionChain &m);

  /** Query Token features are prefixed with qt_* */
  static bool qt_match(QueryEntity qe, streamcorpus::Token t);
  static bool qt_overlap(QueryEntity qe, streamcorpus::Token t);

  /** Mention Chain to Mention Chain features are prefixed with mc_* */
  static bool mc_same_gender(const MentionChain &, const MentionChain &);
  static bool mc_overlap(const MentionChain &m1, const MentionChain &m2);


  /** Entity wide features are prefixed with et_* */
  static bool et_same_tokens(const Entity &); // all mention chains have a match
  static bool et_match_exist(const Entity &); // A match between MentionChains exists
  static bool et_single_mention(const Entity &); // Only on mention is in the set

public:

  /** Get the pairs of ordered Parameter weights */
  static std::vector<std::pair<std::string,double> > const get_params();

  /** Parameters for searching for the propery entity refered by the query entity */
  static std::map<std::string, double> const qm_params;
  static std::map<std::string, double> const qt_params;
  static std::map<std::string, double> const mc_params;
  static std::map<std::string, double> const et_params;

  /** Functions for searching for the propery entity refered by the query entity */
  static std::map<std::string, std::function<bool(QueryEntity, const MentionChain &)> > const qm_functions;
  static std::map<std::string, std::function<bool(QueryEntity, streamcorpus::Token)> > const qt_functions;
  static std::map<std::string, std::function<bool(const MentionChain &, const MentionChain &)> > const mc_functions;
  static std::map<std::string, std::function<bool(const Entity &)> > const et_functions;


  /*const*/ static long SEED_CHAIN;
  /*const*/ static long SEED_ENTITY;
};

#endif  // PARAMETERS_H
