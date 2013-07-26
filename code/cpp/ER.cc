
#include "ER.h"
#include "Parameters.h"
#include "Util.h"

#include <numeric>
#include <random>


std::string ER::pretty_print() {
  std::stringstream ss;

  size_t c = 0;
  for (auto &e: entity) {
    ss << "[" << c++ << "]\n";
    for (auto &m : e.chains) {
      ss <<  "    -  " << to_string( m->tokens(), ", ") << "\n";
    }
  }

  return ss.str();
}

void ER::init() {

  // One entity per mention chain
  // We could possibly make this tuneable in the future
  for (size_t i = 0; i != chains.size(); ++i) {
    Entity e( &chains[i] );
    e.init();
    entity.push_back(e);
  }

  // Initialize the random number generator for selecting entities
  std::default_random_engine generator(Parameters::SEED_ENTITY);
  std::uniform_int_distribution<size_t> entity_distribution(0, entity.size()-1);
  random_entity = std::bind(entity_distribution, generator);
}


double ER::score(const Entity &e1) {

  // Pairwise score all mentions
  double pw_score = 0.0;
  // For all the combination of chains
  for(size_t i = 0; i < chains.size(); ++i) {
    for (size_t j = 0; j <= i; ++j) {
      for (auto k = Parameters::mc_functions.cbegin(); k != Parameters::mc_functions.cend(); ++k) {
        pw_score += ( Parameters::mc_params.at(k->first) ) * ( k->second(chains[i], chains[j])? 1.0 : 0.0 );
      }
    }
  }
  
  // Score all entity features
  double et_score = 0.0;
  for (auto i = Parameters::et_functions.cbegin(); i != Parameters::et_functions.cend(); ++i) {
    et_score += ( Parameters::et_params.at(i->first) ) * ( i->second(e1)? 1.0 : 0.0 );
  }

  return pw_score + et_score;
}


void ER::move(Entity &olde, size_t mc, Entity &newe) {
  MentionChain *m = olde.remove(mc);
  newe.add(m);
}


void ER::undo(Entity &olde, Entity &newe) {
  // We know the last inserted mention should be
  // taken frm the new entity and put in the old.
  MentionChain *m = newe.remove_last();
  olde.add(m);
}


void ER::baseline_er(long samples) {
  size_t retries = 10;

  while (samples-- > 0) {
    log_info("sample: %ld", samples); 
    size_t e_i = random_entity();
    size_t e_j = random_entity();
    
    if (entity[e_i].size() <= 0 || e_i == e_j) { 
      ++samples;
      if (retries-- > 0) continue; else break;
    }

    size_t m = entity[e_i].rand();

    double old_score = score(entity[e_i]) + score(entity[e_j]);
    move(entity[e_i],m,entity[e_j]);
    double new_score = score(entity[e_i]) + score(entity[e_j]);
    log_info("score: [%ld] %f -> [%ld] %f", e_i, old_score, e_j, new_score);

    if(new_score < old_score) { 
      log_info("undo %ld: %ld -> %ld", m, e_i, e_j);
      undo(entity[e_i],entity[e_j]);
    }
    else {
      log_info("accepted! %f", new_score);
    }
  }

  if(retries == 0) log_err("Maximum number of retries reached");
}


int main (int argc, char **argv) {

  // Read in a bunch of examples
  const char *entity_file_name = "../resources/entity/totalEntityList.txt";
  std::ifstream entity_file(entity_file_name, std::fstream::in);
  std::string line;

  // Mention Chains
  std::vector<MentionChain> mcs;

  int limit = 20;
  while (std::getline(entity_file, line) && limit-- > 0 ) {
    std::vector<MentionChain> m(MentionChain::ReadLine(line));
    mcs.insert(mcs.end(), m.begin(), m.end());
    entity_file.clear (); // needed for getline
  }
  entity_file.close();


  // Initialize Mention Chains
  std::for_each(mcs.begin(), mcs.end(), [] (MentionChain& m) {
    m.init();
    //logInfo(m.clean_visible());
    //log_info("--------------------------------------------------------------------------");
  });

  // Create the ER object
  ER er(mcs);
  er.init();

  // Run the baseline ER algorithm for a bunch of samples
  er.baseline_er(50);

  // Print out the result entities
  logInfo( er.pretty_print() );

  return 0;
}

