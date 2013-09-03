#!/usr/bin/python

import io
import json
import pdb
import re
import sys

from django.utils.encoding import smart_str


ENTITYJSONFILE = "../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08i-wiki-alias.json"

ex_re = re.compile(r"^# <(?P<entity>.*) \| (?P<slot_name>.*) \| (?P<slot_value>.*) > -.- (?P<sentence>.*)$") 
entity_re = re.compile(r"(\S+)/(ORG|PER|LOC|FAC)")



def extract_triple(e):
  """ Extract the triple record from the ex_file.
      Returns three strings, representing the entity name, slot name, slot value.
  """
  m = ex_re.match(e)
  if m is None:
    return (None, None, None)
  else:
    return (m.group('entity'), m.group('slot_name'), m.group('slot_value'))


def extract_sentence(e):
  """ Extracts the sentence from the example.
  """
  m = ex_re.match(e)
  if m is None:
    return None
  else:
    return m.group('sentence')


def remove_ner(sentence):
  """ This removes ner information from the sentence string.
      Returns the string without NER info.
  """
  return entity_re.sub(r"\1", sentence) # Replace all entity annotation


def this_entity_type(entity_url):
  """ Returns the string type of the entity in this line.
      >>> this_entity_type ("http://en.wikipedia.org/wiki/Alexander_McCall_Smith")
      "PER"
  """
  with open(ENTITYJSONFILE) as f:
    j = json.load(f)
    result = [k["entity_type"] for k in j["targets"] if k["target_id"] == entity_url]
    if result is not None:
      result[0]

  return None


def should_be_contact_entity(ssf_line, gpg_line, ex_line):
  """ Returns true if this entity is of type FAC and other
      entities exist.
  """
  entity_url = ssf_line.split()[3]
  if this_entity_type(ssf_line) != "FAC":
    return False

  # Look for any entities in the sentence of type PER or ORG
  elist = entity_re.findall(ex_line)
  x = filter(lambda x: x[1] in ("PER","ORG"))

  return len(x) > 0


def change_entity(new_entity, ssf_line):
  """ Change the entity fromfrom whatever it was before to new_entity.
  """
  line = ssf_line.split()
  line[9] = new_entity
  return ' '.join(line)


def byte_range_correction(s,g,e):
  """ Takes an extraction and updates the byte range to include whole sentence.
      It returns the updates ssf_file.
      
  """
  # Extract the current byte range
  (br_s, br_e) = map(int, s.split()[-1].split("-"))

  # Extract the size of the sentence
  sentence = extract_sentence(e)
  sentence = remove_ner(sentence)

  (_,_,slot_value) = extract_triple(e)
  
  # Find the last location location of the slot value
  idx = sentence.rfind(slot_value)

  br_s -= idx
  if br_s < 0:
    br_s = 0
  
  new_ssf = s.split()
  new_ssf[-1] = "%d-%d"%(br_s, br_e)

  return ' '.join(new_ssf)


def check_similar_byte_range(s1,g1,e1,s2,g2,e2):
  """ Compares the old and new byte range, if similar, and other fields are equal return true.

      This returns true if there is an overlap in the byte ranges.
      We only do this if the other fields are equal.
  """
  if s1.split()[:-1] != s2.split()[:-1]:
    return False

  #pdb.set_trace()
  br1 = s1.split()[-1]
  br2 = s2.split()[-1]

  (br1_s, br1_e) = map(int, br1.split("-"))
  (br2_s, br2_e) = map(int, br2.split("-"))

  set1 = set(range(br1_s, br1_e))
  set2 = set(range(br2_s, br2_e))

  return len(set1 & set2) != 0 # If there is overlap then this is similar


def do_dedupe(ssf_file):
  """ Run the dedupe from the file `ssf_file`.

      The ssf_file come in blocks of three lines. The first row 
      is the output line, followed by the gpg|streamitem information.
      And lastly the triple and the sentence.

      Here are the dedupe rules:
        1) If two rows have the same exact output lines
        2) Two rows have the same entity and similar slot_name and slot_value
        3) Check the anchor words (last word in the slot_value) to remove invalid
        ones e.g. "that", or "inn" and others.
  """
  dups = 0
  total_items = 0
  change_entity = 0
  with open(ssf_file, 'r') as f:

    old_ssf_line = None
    old_gpg_line = None
    old_ex_line = None

    #while (f.hasNext()):
    file_iter = iter(f.readline, ' ')
    for line in file_iter:
      if line == '': break

      #pdb.set_trace()
      ssf_line = line
      gpg_line = file_iter.next()
      ex_line = file_iter.next()
      total_items += 1
      
      if (old_ssf_line, old_gpg_line, old_ex_line) == (None, None, None):
        (old_ssf_line, old_gpg_line, old_ex_line) = (ssf_line, gpg_line, ex_line)
        print >> sys.stdout, ssf_line.strip()
        print >> sys.stdout, gpg_line.strip()
        print >> sys.stdout, ex_line.strip()
        continue

      # If ssf_lines are the same
      if ssf_line == old_ssf_line:
        dups += 1
        continue

      # If it is Contact_Meet_PlaceTime, update the byte range
      if "Contact_Meet_PlaceTime" in ssf_line:
        ssf_line = byte_range_correction(ssf_line, gpg_line, old_ex_line)

      # If all is the same except for a small variation in the byte range
      if not check_similar_byte_range(ssf_line, gpg_line, ex_line, old_ssf_line,\
        old_gpg_line, old_ex_line):
        dups += 1
        continue
      
      # If this is a Contact_Meet_PlaceTime, could it also be a Contact_Meet_Entity
      if "Contact_Meet_Entity" in ssf_line and\
        should_be_contact_entity(ssf_line, gpg_line, ex_line):

        new_ssf_line = change_entity("Contact_Meet_Entity", ssf_line)
        print >> sys.stdout, new_ssf_line
        print >> sys.stdout, gpg_line
        print >> sys.stdout, ex_line
        change_entity += 1


      (old_ssf_line, old_gpg_line, old_ex_line) = (ssf_file, gpg_line, ex_line)
      print >> sys.stdout, ssf_line
      print >> sys.stdout, gpg_line
      print >> sys.stdout, ex_line

  return (total_items, dups, change_entity)

    

if __name__ == '__main__':
  usage = """
    Takes an SSF output file as input, and removes duplicate results.
    The output is printed to stdout.

      Usage: python ssf_dedupe.py <ssf_file>
    """
 
  if (len(sys.argv) > 2 or len(sys.argv) <= 1):
    print sys.stderr, "%s" % usage
    exit(-1)

  ssf_file = sys.argv[-1]
  (total_items, dups, change_entity) = do_dedupe(ssf_file)

  print >> sys.stderr, "\nRead %d items, found %d duplicates and changed %d entities." % (total_items, dups, change_entity)

