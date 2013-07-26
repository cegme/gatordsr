
#ifndef ER_H
#define ER_H

#include "Entity.h"
#include "MentionChain.h"

#include <vector>


class ER {
  friend class Entity;

private:
  // All the Mention Chains in this entity
  std::vector<MentionChain> chains;

  // All the Entities for this ER process
  std::vector<Entity> entity;

  std::function<size_t()> random_entity;

public:
  ER():chains(std::vector<MentionChain>()), entity(std::vector<Entity>()) { }
  ER(std::vector<MentionChain> _chains): chains(_chains), entity(std::vector<Entity>()) { }

  std::string pretty_print() ;

  double score(const Entity &e1);
  void move(Entity &olde, size_t mc, Entity &newe);

  /** Take the last inserted entity in newe and put it in olde */
  void undo(Entity &olde, Entity &newe);

  /** Initialize the random number generators and set all the chains to entities.
    * This needs to be called before running the algo.
   */
  void init();

  void baseline_er(long samples); 
};






#endif  // ER_H
