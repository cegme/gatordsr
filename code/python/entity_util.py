#!/usr/bin/python

import codecs
import io

import json
from json import dumps as dumps


from django.utils.encoding import smart_str

# Run this from the gatordsr/code/python/ folder
ENTITYJSONFILE = "../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json"
ENTITYVECTORFILE = "../cpp/entity_vector.cpp"

def print_entity_list():
  with open(ENTITYJSONFILE) as f:
    j = json.load(f)
    for i,k in enumerate(j["targets"]):
      for alias in k["alias"]:
        row = u"%s|%s|%s|%s" % (k["target_id"], k["group"], k["entity_type"], alias)
        print smart_str(row)


def create_c11_list():
  header = """#include <vector>\n#include "entity_match.h"\n"""
  namespace_open = """namespace streamcorpus {\n"""
  init = """  std::vector<streamcorpus::found_entity> alias_vector = {\n"""
  namespace_close = " }; // streamcorpus \n"
  footer = "  };\n"
  with io.open(ENTITYJSONFILE, 'r', encoding="utf-8") as f:
    j = json.load(f, encoding="utf-8")
    row = []
    for i,k in enumerate(j["targets"]):
      for alias in k["alias"]:
        alias = alias.replace('"', '\\"')
        row += ["""    streamcorpus::found_entity("%s", "%s", "%s", "%s")""" % (k["target_id"], k["group"], k["entity_type"], alias) ]
    print smart_str(header)
    print smart_str(namespace_open)
    print smart_str(init)
    print smart_str(",\n".join(row))
    print smart_str(footer)
    print smart_str(namespace_close)


if __name__ == '__main__':
  """
    Prints all the aliases. The schema is target_id|group|entity_type|alias

    Usage: python entity_util.py ../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json <-t>

    <-t> is any character will out put the cpp file instead of the entity list
  """
  import sys

  ENTITYJSONFILE = ENTITYJSONFILE if len(sys.argv) < 2 else sys.argv[1]
    
  if (len(sys.argv) < 3):
    print_entity_list()
  else:
    create_c11_list()
