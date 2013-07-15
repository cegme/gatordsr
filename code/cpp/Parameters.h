
#ifndef PARAMETERS_H
#define PARAMETERS_H

#include <functional>
#include <map>
#include <string>

#include "streamcorpus_constants.h"
#include "streamcorpus_types.h"


#include "QueryEntity.h"

class Parameters {

private:
  static bool qt_match(QueryEntity qe, streamcorpus::Token t);
  static bool qt_overlap(QueryEntity qe, streamcorpus::Token t);


public:
  /** Parameters for searching for the propery entity refered by the query entity */
  static std::map<std::string, double> const qt_params;

  /** Functions for searching for the propery entity refered by the query entity */
  static std::map<std::string, std::function<bool(QueryEntity, streamcorpus::Token)> > const qt_functions;



  const static long SEED_CHAIN;
  const static long SEED_ENTITY;
};

#endif  // PARAMETERS_H
