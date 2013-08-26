#ifndef UTIL_H
#define UTIL_H

#include <algorithm>
#include <sstream>
#include <vector>

#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <boost/algorithm/string.hpp>
#include "boost/algorithm/string_regex.hpp"
#include "boost/date_time/gregorian/gregorian.hpp"
#include "boost/date_time/posix_time/posix_time.hpp"
#include <boost/date_time/time_clock.hpp>
#include "/usr/include/boost/date_time/microsec_time_clock.hpp"
#include <sys/stat.h>


/** Check to see if a file exists */
static bool fexist (const char *fileName) { 
  struct stat buffer;
  return (stat (fileName, &buffer) == 0);
}

/** Write out a stirng array */
inline static std::string to_string(std::vector<std::string> v, std::string sep) {
  std::stringstream ss;
  if (v.empty()) return "";
  for (auto i = v.begin(); (i+1) != v.end(); ++i) { 
    ss << *i << sep;
  }
  ss << v.back();
  return ss.str();
}

/** tokenize wulti string separators */
static std::vector<std::string> tok(std::string str, std::string sep) {

  std::vector<std::string> results;
  //boost::split(results, str, [sep] (std::string _sep) { return _sep == sep; });
  boost::algorithm::split_regex(results, str, boost::regex(sep));
  //boost::split(results, str, boost::is_any_of(", "));

  return results;
}

#define DATE_STRING boost::posix_time::to_simple_string( boost::date_time::microsec_clock<boost::posix_time::ptime>::local_time() ).c_str()

#ifdef NDEBUG
#define debug(M, ...)
#else
#define debug(M, ...) fprintf(stderr, "DEBUG %s:%d: " M "\n", __FILE__, __LINE__, ##__VA_ARGS__)
#endif

#define clean_errno() (errno == 0 ? "None" : strerror(errno))

// This is for cpp strings
#define logInfo(M) std::cerr << DATE_STRING << "[INFO] (" << __FILE__ << ":" << __LINE__ << ") | " << M  << "\n"

#define log_info(M, ...) fprintf(stderr, "%s [INFO] (%s:%d) | " M "\n", DATE_STRING,  __FILE__, __LINE__, ##__VA_ARGS__)

#define log_trace(M, ...) fprintf(stderr, "%s [trace] (%s:%d) | " M "\n", DATE_STRING,  __FILE__, __LINE__, ##__VA_ARGS__)

#define log_debug(M, ...) fprintf(stderr, "%s [DEBUG] (%s:%d) | " M "\n", DATE_STRING,  __FILE__, __LINE__, ##__VA_ARGS__)

#define log_err(M, ...) fprintf(stderr, "%s [ERROR] (%s:%d: errno: %s) | " M "\n", DATE_STRING, __FILE__, __LINE__, clean_errno(), ##__VA_ARGS__)

#define log_warn(M, ...) fprintf(stderr, "%s [WARN] (%s:%d: errno: %s) | " M "\n", DATE_STRING, __FILE__, __LINE__, clean_errno(), ##__VA_ARGS__)

#define check_mem(A) check((A), "Out of memory.")

#define MAX(a,b) ( ((a) > (b)) ? (a) : (b) )

// Do a carrage return after these lines so they will rewrite echother
#define log_info_r(M, ...) fprintf(stderr, "\r%s [INFO] (%s:%d) | " M , DATE_STRING,  __FILE__, __LINE__, ##__VA_ARGS__)

#define log_trace_r(M, ...) fprintf(stderr, "\r%s [trace] (%s:%d) | " M , DATE_STRING,  __FILE__, __LINE__, ##__VA_ARGS__)

#define log_debug_r(M, ...) fprintf(stderr, "\r%s [DEBUG] (%s:%d) | " M , DATE_STRING,  __FILE__, __LINE__, ##__VA_ARGS__)

#define log_err_r(M, ...) fprintf(stderr, "\r%s [ERROR] (%s:%d: errno: %s) | " M , DATE_STRING, __FILE__, __LINE__, clean_errno(), ##__VA_ARGS__)

#define log_warn_r(M, ...) fprintf(stderr, "\r%s [WARN] (%s:%d: errno: %s) | " M , DATE_STRING, __FILE__, __LINE__, clean_errno(), ##__VA_ARGS__)





#endif  // UTIL_H

