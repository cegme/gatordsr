
#include "ER.h"
#include "Parameters.h"

#include <random>


void ER::init() {

  // Initialize the random number generator for selecting entities
  std::default_random_engine generator(Parameters::SEED_ENTITY);
  std::uniform_int_distribution<size_t> entity_distribution(0, entity.size());
  random_entity = std::bind(entity_distribution, generator);

}


double ER::score(const Entity &e1) {

  // TODO 
  return 0.0;
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

  while (samples-- > 0) {
    size_t e_i = random_entity();
    size_t e_j = random_entity();
    size_t m = entity[e_i].rand();

    double old_score = score(entity[e_i]) + score(entity[e_j]);

    move(entity[e_i],m,entity[e_j]);
    
    double new_score = score(entity[e_i]) + score(entity[e_j]);

    if(new_score < old_score) { 
      // UNDO the the changes, the model was not improved.
      undo(entity[e_i],entity[e_j]);
    }

  }
}


