
#include <algorithm>

#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string/case_conv.hpp>

#include "Parameters.h"
#include "Util.h"


std::vector<std::pair<std::string, double> > const Parameters::get_params() {

  std::vector<std::pair<std::string, double> > params;
  
  // TODO params.insert(params.end(), qm_params.cbegin(), qm_params.cend());
  params.insert(params.end(), mc_params.cbegin(), mc_params.cend());
  params.insert(params.end(), et_params.cbegin(), et_params.cend());

  return params;
}

////////////////////////////////////////////////////////////////////////////////


std::map<std::string, double> const Parameters::qm_params = {
  {"qe_match", 500.0},
  {"jaccard_80", 100.0},
  {"jaccard_90", 200.0}
};

bool Parameters::qm_qe_match(QueryEntity qe, const MentionChain &m) {
  return qe.target_id == m.qe.target_id;
}

bool Parameters::qm_jaccard_80(QueryEntity qe, const MentionChain &m) {
  // Get both Vvectors and sort it 
  std::vector<std::string> a = qe.wikipedia_tokens;
  std::vector<std::string> b = m.tokens();

  // Sort vectors
  std::sort(a.begin(), a.end());
  std::sort(b.begin(), b.end());
  
  // Union the vocabulary of both vectors
  // M11
  std::vector<std::string> m11(a.size()+b.size());
  auto it11 = std::set_union (a.begin(), a.end(), b.begin(), b.end(), m11.begin());
  m11.resize(it11 - m11.begin());

  // M01
  std::vector<std::string> m01(a.size()+b.size());
  auto it01 = std::set_difference (a.begin(), a.end(), b.begin(), b.end(), m01.begin());
  m01.resize(it01 - m01.begin());

  // M10
  std::vector<std::string> m10(a.size()+b.size());
  auto it10 = std::set_difference (a.begin(), a.end(), b.begin(), b.end(), m10.begin());
  m10.resize(it10 - m10.begin());

  
  // Is this greater than 0.8
  log_info("result jaccard_80: %f", (double) m11.size()/(m11.size() + m10.size() + m01.size()));
  return (m11.size()/ (m11.size() + m10.size() + m01.size())) > .8 ? true:false;
}

bool Parameters::qm_jaccard_90(QueryEntity qe, const MentionChain &m) {
  // Get both Vvectors and sort it 
  std::vector<std::string> a = qe.wikipedia_tokens;
  std::vector<std::string> b = m.tokens();

  // Sort vectors
  std::sort(a.begin(), a.end());
  std::sort(b.begin(), b.end());
  
  // Union the vocabulary of both vectors
  // M11
  std::vector<std::string> m11(a.size()+b.size());
  auto it11 = std::set_union (a.begin(), a.end(), b.begin(), b.end(), m11.begin());
  m11.resize(it11 - m11.begin());

  // M01
  std::vector<std::string> m01(a.size()+b.size());
  auto it01 = std::set_difference (a.begin(), a.end(), b.begin(), b.end(), m01.begin());
  m01.resize(it01 - m01.begin());

  // M10
  std::vector<std::string> m10(a.size()+b.size());
  auto it10 = std::set_difference (a.begin(), a.end(), b.begin(), b.end(), m10.begin());
  m10.resize(it10 - m10.begin());

  
  // Is this greater than 0.9
  log_debug("result jaccard_90: %f", (double) m11.size()/(m11.size() + m10.size() + m01.size()));
  return (m11.size()/ (m11.size() + m10.size() + m01.size())) > .9 ? true:false;
}


std::map<std::string, std::function<bool(QueryEntity, const MentionChain &)> > const Parameters::qm_functions = {
  {"qe_match", Parameters::qm_qe_match},
  {"jaccard_80", Parameters::qm_jaccard_80},
  {"jaccard_90", Parameters::qm_jaccard_90}
};
////////////////////////////////////////////////////////////////////////////////

std::map<std::string, double> const Parameters::qt_params = {
  {"match", 100.0},
  {"overlap", 150.0}
};


std::map<std::string, std::function<bool(QueryEntity, streamcorpus::Token)> > const Parameters::qt_functions = {
  {"match", Parameters::qt_match},
  {"overlap", Parameters::qt_overlap}
};

bool Parameters::qt_match(QueryEntity qe, streamcorpus::Token t) {
  return std::any_of(qe.aliases.cbegin(), qe.aliases.cend(), [t] (std::string alias) {
    //log_info("istarts_with: %s --- %s", t.token.c_str(), alias.c_str());
    return boost::algorithm::istarts_with(alias, t.token) ||
      boost::algorithm::istarts_with(t.token, alias);
    //return boost::iequals(t.token, alias);
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
  {"same_gender", Parameters::mc_same_gender},
  {"overlap", Parameters::mc_overlap}
};

std::map<std::string, double> const Parameters::mc_params = {
  {"same_gender", 3.0},
  {"overlap", 125.0}
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
  return (m1_male > m1_female) == (m2_male > m2_female) || 
    (m1_male == m1_female) == (m2_male == m2_female);
}


bool Parameters::mc_overlap(const MentionChain &m1, const MentionChain &m2) {
  for (auto t1 : m1.tokens()) {
    for (auto t2 : m2.tokens()) {
      if (boost::iequals(t1, t2)) {
        return true;
      }
    }
  }
  return false;
}

////////////////////////////////////////////////////////////////////////////////

std::map<std::string, double> const Parameters::et_params = {
  {"same_tokens", 90.0},
  {"match_exists", 45.0},
  {"single_mention", -0.0}
};


std::map<std::string, std::function<bool(const Entity &)> > const Parameters::et_functions = {
  {"same_tokens", Parameters::et_same_tokens},
  {"match_exists", Parameters::et_match_exist},
  {"single_mention", Parameters::et_single_mention}
};


bool Parameters::et_same_tokens(const Entity &e1) {
  
  // Check to see that all the mention chains have an overlap 
  if (e1.size() <= 1) return false;
  for (size_t i = 1; i < e1.size(); ++i) {
    for (size_t j = 0; j < i; ++j) {
      if (i==j) continue;
      std::vector<std::string> m1 = e1.chains[i]->tokens();
      e1.chains[j]->init();
      std::vector<std::string> m2 = e1.chains[j]->tokens();
      
      std::sort(m1.begin(), m1.end());
      std::sort(m2.begin(), m2.end());

      std::vector<std::string> overlap;
      //std::vector<std::string> overlap(MAX(m1.size(),m2.size()));
      //auto it = std::set_intersection(m1.begin(), m1.end(), m2.begin(), m2.end(), overlap.begin());
      auto it = std::set_intersection(m1.begin(), m1.end(), m2.begin(), m2.end(), std::back_inserter(overlap));
      log_info("m1:: %s", to_string(m1,",").c_str());
      log_info("m2:: %s", to_string(m2, ",").c_str());
      if (overlap.empty()) { log_info ("no_overlap"); return false; }
      else  log_info ("yes_overlap: %s", to_string(overlap, "+").c_str()); 
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
      //std::vector<std::string> overlap;
      auto it = std::set_intersection(m1.begin(), m1.end(), m2.begin(), m2.end(), overlap.begin());
      overlap.resize(it-overlap.begin());

      if (!overlap.empty()) {
        //log_info("overlap found: %s", e1.pretty_print().c_str() );
        log_info("overlap: %s", to_string(overlap, "::").c_str());
        return true;
      }
    }
  }
  return false;
}


bool Parameters::et_single_mention(const Entity & e) {
  return e.size() == 1;
}




/*const*/ long Parameters::SEED_CHAIN = 42;
/*const*/ long Parameters::SEED_ENTITY = 42;


