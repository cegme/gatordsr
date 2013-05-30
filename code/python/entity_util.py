#!/usr/bin/python

import codecs
import json

from django.utils.encoding import smart_str

ENTITYJSONFILE = "../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json"

def print_entity_list():
  with open(ENTITYJSONFILE) as f:
    j = json.load(f)
    for i,k in enumerate(j["targets"]):
      for alias in k["alias"]:
        row = u"%s|%s|%s|%s" % (k["target_id"], k["group"], k["entity_type"], alias)
        print smart_str(row)


if __name__ == '__main__':
  """
    Prints all the aliases. The schema is target_id|group|entity_type|alias

    Usage: python entity_util.py ../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08.json
  """
  import sys

  jsonfile = ENTITYJSONFILE if len(sys.argv) < 1 else sys.argv[-1]
    
  print_entity_list()

