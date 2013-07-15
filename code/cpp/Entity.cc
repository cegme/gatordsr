
#include "Entity.h"
#include "Parameters.h"

#include <algorithm>
#include <random>


MentionChain* Entity::remove(size_t mention_idx) {
  MentionChain *m = chains[mention_idx];
  chains.erase(chains.begin()+mention_idx);
  init();
  return m;
}

MentionChain * Entity::remove_last() {
  MentionChain *m = chains[chains.size()-1];
  chains.erase(chains.end()-1);
  init();
  return m;
}

void Entity::init() {

  // Initialize the random number generator for selecting mention chains
  std::default_random_engine generator(Parameters::SEED_CHAIN);
  std::uniform_int_distribution<size_t> chain_distribution(0, chains.size());
  random_chain = std::bind(chain_distribution, generator);

}

void Entity::add(MentionChain *m) {
  chains.push_back(m); 
  init();
}

size_t Entity::rand() {
  return random_chain();
}

