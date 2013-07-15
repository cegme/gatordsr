
#ifndef Entity_H
#define Entity_H

#include "MentionChain.h"

#include <vector>

class Entity {

//private:
public:
  std::vector<MentionChain*> chains;
  std::function<size_t()> random_chain;

  MentionChain * remove_last();
  
public:
  MentionChain * remove(size_t mentionm_idx);
  void add(MentionChain *m);

  /** Initialize the random number generaators */
  void init();

  /** Return the index of a random mention chain */
  size_t rand();

};






#endif  // Entity_H
