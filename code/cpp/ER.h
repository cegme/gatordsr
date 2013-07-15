
#ifndef ER_H
#define ER_H

#include "Entity.h"
#include "MentionChain.h"

#include <vector>


class ER {
  friend class Entity;

private:
  std::vector<MentionChain> chains;
  std::vector<Entity> entity;

  std::function<size_t()> random_entity;

public:
  double score(const Entity &e1);
  void move(Entity &olde, size_t mc, Entity &newe);

  /** Take the last inserted entity in newe and put it in olde */
  void undo(Entity &olde, Entity &newe);

  /** Initialize the random number generaators */
  void init();

  void baseline_er(long samples); 
};






#endif  // ER_H
