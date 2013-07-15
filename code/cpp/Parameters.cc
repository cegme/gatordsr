
#include <algorithm>

#include <boost/algorithm/string.hpp>

#include "Parameters.h"

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


const long Parameters::SEED_CHAIN = 42;
const long Parameters::SEED_ENTITY = 42;


