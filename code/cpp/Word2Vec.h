
#ifndef WORD2VEC_H
#define WORD2VEC_H

#include "QueryEntity.h"
#include "MentionChain.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <pthread.h>

#define MAX_STRING 300
#define EXP_TABLE_SIZE 1000
#define MAX_EXP 6
#define MAX_SENTENCE_LENGTH 1000
#define MAX_CODE_LENGTH 40


class Word2Vec {

public:
  Word2Vec(): 
    binary(1), cbow(0), debug_mode(2), window(15), min_count(0), num_threads(16), min_reduce(1),
    vocab_max_size(1000), vocab_size(0), layer1_size(100),
    train_words(0), word_count_actual(0), file_size(0), classes(0),
    alpha(0.025), sample(0),
    hs(1), negative(0),
    vocab_hash_size(300000),
    table_size(1e8) 
  { 
    init();
  }
    
  //constexpr static int vocab_hash_size = 300000;  // Maximum 30 * 0.7 = 21M words in the vocabulary
  int vocab_hash_size;  // Maximum 30 * 0.7 = 21M words in the vocabulary

  typedef float real;                    // Precision of float numbers

  struct vocab_word {
    long long cn;
    int *point;
    char *word, *code, codelen;
    bool operator<(const struct vocab_word &a) const {
      return !(cn < a.cn); // Need the ! to work with C code
    }
  };

  char train_file[MAX_STRING], output_file[MAX_STRING];
  char save_vocab_file[MAX_STRING], read_vocab_file[MAX_STRING];
  struct vocab_word *vocab;
  int binary, cbow, debug_mode, window, min_count, num_threads, min_reduce;
  int *vocab_hash;
  long long vocab_max_size, vocab_size, layer1_size;
  long long train_words, word_count_actual, file_size, classes;
  real alpha, starting_alpha, sample;
  real *syn0, *syn1, *syn1neg, *expTable;
  clock_t start;

  int hs, negative;
  //constexpr static int table_size = 1e8;
  int table_size ;
  int *table;

  // Return a list of the closest words to the 'word'.  N is the max number of similar wards to return
  std::vector<std::pair<std::string, float> > distance(char * word, const long long N);
  std::vector<std::pair<std::string, float> > distance(MentionChain *mc, const long long N);
  std::vector<std::pair<std::string, float> > distance(QueryEntity *qe, const long long N);

  float PairwiseSimilarity(char *word1, char *word2);
  //bool LoadProjectionFile(char *file_name, bool binary);
  
  void InitUnigramTable();

  // Reads a single word from a file, assuming space + tab + EOL to be word boundaries
  void ReadWord(char *word, FILE *fin) ;

  // Returns hash value of a word
  int GetWordHash(char *word);

  // Returns position of a word in the vocabulary; if the word is not found, returns -1
  int SearchVocab(char *word);

  // Reads a word and returns its index in the vocabulary
  int ReadWordIndex(FILE *fin) ;
   
  // Adds a word to the vocabulary
  int AddWordToVocab(char *word) ;
   
  // Used later for sorting by word counts
  int VocabCompare(const void *a, const void *b) ;

  // Sorts the vocabulary by frequency using word counts
  void SortVocab();

  // Reduces the vocabulary by removing infrequent tokens
  void ReduceVocab();

  // Create binary Huffman tree using the word counts
  // Frequent words will have short uniqe binary codes
  void CreateBinaryTree();

  void LearnVocabFromTrainFile();

  void SaveVocab();

  void ReadVocab();

  void InitNet();

  struct pthread_params { long a; Word2Vec *w; };
  static void  *TrainModelThreadHelper(void *obj_id);
  void *TrainModelThread(void *id);

  void TrainModel();

  void init();

  int ArgPos(char *str, int argc, char **argv);

  int Main(int argc, char **argv) ;

};


#endif // WORD2VEC_H

