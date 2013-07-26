
#ifndef Entity_H
#define Entity_H

#include "MentionChain.h"

#include <vector>

class Entity {

public:
  Entity(): chains(std::vector<MentionChain*>()) { }
  Entity(MentionChain *mc): chains({mc}) { }
  Entity(std::vector<MentionChain *> mcs): chains(mcs) { }

  std::vector<MentionChain*> chains;
  std::function<size_t()> random_chain;

  MentionChain * remove_last();
  MentionChain * remove(size_t mentionm_idx);
  void add(MentionChain *m);

  /** Initialize the random number generaators */
  void init();

  /** Return the index of a random mention chain */
  size_t rand();

  /** Return the number of chains */
  size_t size();
};






#endif  // Entity_H
