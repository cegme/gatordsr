
#include <algorithm>

#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string/case_conv.hpp>

#include "Parameters.h"
#include "Util.h"

std::map<std::string, double> const Parameters::qt_params = {
  {"match", 100.0},
  {"overlap", 50.0}
};


std::map<std::string, std::function<bool(QueryEntity, streamcorpus::Token)> > const Parameters::qt_functions = {
  {"match", Parameters::qt_match},
  {"overlap", Parameters::qt_overlap}
};

bool Parameters::qt_match(QueryEntity qe, streamcorpus::Token t) {
  return std::any_of(qe.aliases.cbegin(), qe.aliases.cend(), [t] (std::string alias) {
    return boost::iequals(t.token, alias);
  });
}


bool Parameters::qt_overlap(QueryEntity qe, streamcorpus::Token t) {
  typedef const boost::iterator_range<std::string::const_iterator> StringRange;

  return std::any_of(qe.aliases.cbegin(), qe.aliases.cend(), [t] (std::string alias) {
    return boost::ifind_first(
      StringRange(t.token.begin(), t.token.end()),
      StringRange(alias.begin(), alias.end()));
    //return boost::ifind_first(t.token, alias) != std::string::npos;
  });
}


////////////////////////////////////////////////////////////////////////////////

std::map<std::string, std::function<bool(const MentionChain &, const MentionChain &)> > const Parameters::mc_functions = {
  {"same_gender", Parameters::mc_same_gender}
};

std::map<std::string, double> const Parameters::mc_params = {
  {"same_gender", 9.0}
};

std::set<std::string> male = {"his", "him", "he", "man", "mister", "mr"};
std::set<std::string> female = {"her", "she", "hers", "ms", "mrs", "woman"};
bool Parameters::mc_same_gender(const MentionChain &m1, const MentionChain &m2) {
  // Look for clues in both for genders see if they probably match
  int m1_male = 0;
  int m1_female = 0;
  int m2_male = 0;
  int m2_female = 0;

  for (size_t i = 0; i != m1.tokencount(); ++i) {
    std::string s = m1.get(i);
    boost::algorithm::to_lower(s);
    if (male.find(s)!= male.end()) m1_male += 1;
    if (female.find(s)!= female.end()) m1_female += 1;
  }
  for (size_t i = 0; i != m2.tokencount(); ++i) {
    std::string s = m2.get(i);
    boost::algorithm::to_lower(s);
    if (male.find(s)!= male.end()) m2_male += 1;
    if (female.find(s)!= female.end()) m2_female += 1;
  }
  //log_info("%d %d %d %d", m1_male, m1_female, m2_male, m2_female);
  // Either they both have the same gender
  return (m1_male >= m1_female) == (m2_male >= m2_female);
}


////////////////////////////////////////////////////////////////////////////////

std::map<std::string, double> const Parameters::et_params = {
  {"same_tokens", 70.0},
  {"match_exists", 15.0}
};


std::map<std::string, std::function<bool(const Entity &)> > const Parameters::et_functions = {
  {"same_tokens", Parameters::et_same_tokens},
  {"match_exists", Parameters::et_match_exist}
};


bool Parameters::et_same_tokens(const Entity &e1) {
  
  // Check to see that all the mention chains have an overlap 
  for (size_t i = 1; i < e1.chains.size(); ++i) {
    for (size_t j = 0; j < i; ++j) {
      if (i==j) continue;
      std::vector<std::string> m1 = e1.chains[i]->tokens();
      //logInfo( to_string(m1, ", ") ); 
      e1.chains[j]->init();
      std::vector<std::string> m2 = e1.chains[j]->tokens();
      
      std::sort(m1.begin(), m1.end());
      std::sort(m2.begin(), m2.end());

      std::vector<std::string> overlap(MAX(m1.size(),m2.size()));
      auto it = std::set_intersection(m1.begin(), m1.end(), m2.begin(), m2.end(), overlap.begin());
      if (overlap.empty()) return false;
    }
  }

  return true;
}

bool Parameters::et_match_exist(const Entity & e1) {
 // A match between MentionChains exists
  for (size_t i = 1; i < e1.chains.size(); ++i) {
    for (size_t j = 0; j < i; ++j) {
      if (i==j) continue;
      std::vector<std::string> m1 = e1.chains[i]->tokens();
      //logInfo( to_string(m1, ", ") ); 
      e1.chains[j]->init();
      std::vector<std::string> m2 = e1.chains[j]->tokens();
      
      std::sort(m1.begin(), m1.end());
      std::sort(m2.begin(), m2.end());

      std::vector<std::string> overlap(MAX(m1.size(),m2.size()));
      auto it = std::set_intersection(m1.begin(), m1.end(), m2.begin(), m2.end(), overlap.begin());
      if (!overlap.empty()) return true;
    }
  }
  return false;
}

const long Parameters::SEED_CHAIN = 42;
const long Parameters::SEED_ENTITY = 42;

