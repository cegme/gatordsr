#!/usr/bin/python

import argparse
import sre_constants
import collections
try:
  from collections import Counter
except ImportError:
  try:
    import counter
    from counter import Counter
  except ImportError:
    pass

import io
import json
import math
import pdb
import re
import sys

#from django.utils.encoding import smart_str


ENTITYJSONFILE = "../resources/entity/trec-kba-ccr-and-ssf-query-topics-2013-04-08-wiki-alias.json"
ex_re = re.compile(r"^# <(?P<entity>.*)\|(?P<slot_name>.*)\|(?P<slot_value>.*)> -.- (?P<sentence>.*)$") 
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
    print >> sys.stderr, "Error with a sentence: %s"%e
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
      #print >> sys.stderr, "Result: %s, entity_url: %s"  % (result, entity_url)
      return result[0]

  return None


def should_be_contact_entity(ssf_line, gpg_line, ex_line):
  """ Returns true if this entity is of type FAC and other
      entities exist.
  """
  entity_url = ssf_line.split()[3]
  if this_entity_type(entity_url) != "FAC":
    return False

  # Look for any entities in the sentence of type PER or ORG
  elist = entity_re.findall(ex_line)
  x = filter(lambda x: x[1] in ("PER","ORG"), elist)

  return len(x) > 0


def transform_entity(new_entity, ssf_line):
  """ Change the entity fromfrom whatever it was before to new_entity.
      Returns the new entity.
  """
  line = ssf_line.split()
  line[8] = new_entity
  return ' '.join(line)


def minimum_anchorword_distance(s,g,e):
  """ Finds the minimum distance between the entity and the slot name.
  """
  try:
    sentence = extract_sentence(e)
    sentence = remove_ner(sentence)

    (entity, slot_name, slot_value) = extract_triple(e)

    eidx_search = re.search(entity, sentence, flags=re.IGNORECASE)
    svidx_search = re.search(slot_value, sentence, flags=re.IGNORECASE)
   
    if eidx_search is None or svidx_search is None:
      #print >> sys.stderr, "Could not find the entity or slot in the sentence"
      return 0

    eidx = [m.start() for m in re.finditer(entity, sentence, flags=re.IGNORECASE)]
    svidx= [m.start() for m in re.finditer(slot_value, sentence, flags=re.IGNORECASE)]

    if len(eidx) == 0  or len(svidx) == 0:
      #print >> sys.stderr, "Could not find the entity or slot in the sentence"
      return 0

    min_val = min([math.fabs(i-j) for i in eidx for j in svidx])
    return min_val
  except sre_constants.error:
    #print >> sys.stderr, "Error compiling the expression"
    return 0


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

  br_s -= idx - sentence.count(' ', 0, idx)
  if br_s < 0:
    br_s = 0
  
  new_ssf = s.split()
  new_ssf[-1] = "%d-%d"%(br_s, br_e)

  return ' '.join(new_ssf)


def is_similar_byte_range(s1, s2):
  """ Compares the old and new byte range, if similar, and other fields are equal return true.

      This returns true if there is an overlap in the byte ranges.
      We only do this if the other fields are equal.
  """
  #pdb.set_trace()
  br1 = s1.split()[-1]
  br2 = s2.split()[-1]

  (br1_s, br1_e) = map(int, br1.split("-"))
  (br2_s, br2_e) = map(int, br2.split("-"))

  set1 = set(range(br1_s, br1_e))
  set2 = set(range(br2_s, br2_e))

  return len(set1 & set2) != 0 # If there is overlap then this is similar


def print_differences(old_ssf_line, old_gpg_line, old_ex_line, ssf_line, gpg_line, ex_line):

  for ssf in enumerate(zip(old_ssf_line.split(), ssf_line.split())):
    if ssf[1][0] != ssf[1][1]:
      print >> sys.stderr, "ssf_line %d: %s==%s" % (ssf[0], ssf[1][0], ssf[1][1])
  
  if old_gpg_line.split("|")[0] !=  gpg_line.split("|")[0]:
    print >> sys.stderr, "gpgline 0: %d==%d"%(old_gpg_line.split("|")[0],gpg_line.split("|")[0])
  if old_gpg_line.split("|")[1] !=  gpg_line.split("|")[1]:
    print >> sys.stderr, "gpg_line 1: %d==%d"%(old_gpg_line.split("|")[1],gpg_line.split("|")[1])

  olde = ex_re.match(old_ex_line)
  newe = ex_re.match(ex_line)
  if olde.group("entity") != newe.group("entity"):
    print >> sys.stderr, "ex_line: (entity) %s==%s"%(olde.group("entity"), newe.group("entity"))

  if olde.group("slot_name") != newe.group("slot_name"):
    print >> sys.stderr, "ex_line: (slot_name) %s==%s"%(olde.group("slot_name"), newe.group("slot_name"))

  if olde.group("slot_value") != newe.group("slot_value"):
    print >> sys.stderr, "ex_line: (slot_value) %s==%s"%(olde.group("slot_value"), newe.group("slot_value"))

  # if olde.group("sentence") != newe.group("sentence"):
    # print >> sys.stderr, "ex_line: (sentence) %s==%s"%(olde.group("sentence"), newe.group("sentence"))
      

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
  too_long = 0
  anchor_distance = 0
  with open(ssf_file, 'r') as f:

    old_ssf_line = None
    old_gpg_line = None
    old_ex_line = None

    file_iter = iter(f.readline, ' ')
    for line in file_iter:
      if line == '': break

      ssf_line = line
      gpg_line = file_iter.next()
      ex_line = file_iter.next()
      total_items += 1

      # Ignore this entry if the max entry is less than the threshold
      if minimum_anchorword_distance(ssf_line, gpg_line, ex_line) > 273:
        print >> sys.stderr, ssf_line.strip()
        print >> sys.stderr, gpg_line.strip()
        print >> sys.stderr, ex_line.strip()
        anchor_distance += 1
        continue
        

      # Ignore really long files over this many characters
      if len(extract_sentence(ex_line)) > 2000 and\
        ("Boris" in ssf_line or "Basic_Element" in ssf_line) :
        #print >> sys.stderr, ssf_line.strip()
        #print >> sys.stderr, gpg_line.strip()
        #print >> sys.stderr, ex_line.strip()
        too_long +=1 
        continue

      if (old_ssf_line, old_gpg_line, old_ex_line) == (None, None, None):
        (old_ssf_line, old_gpg_line, old_ex_line) = (ssf_line, gpg_line, ex_line)
        print >> sys.stdout, ssf_line.strip()
        print >> sys.stdout, gpg_line.strip()
        print >> sys.stdout, ex_line.strip()
        continue

      # Use this to debug and see the diferences in the adjacent records
      # print_differences(old_ssf_line, old_gpg_line, old_ex_line, ssf_line, gpg_line, ex_line)

      # If ssf_lines are the same
      if ssf_line == old_ssf_line:
        dups += 1
        continue

      # If it is Contact_Meet_PlaceTime, update the byte range
      if "Contact_Meet_PlaceTime" == ssf_line.split()[8]:
        ssf_line = byte_range_correction(ssf_line, gpg_line, ex_line)

      # If all is the same except for a small variation in the byte range
      if ssf_line.split()[:-1] == old_ssf_line.split()[:-1] and \
        not is_similar_byte_range(ssf_line, old_ssf_line):
        dups += 1
        continue
      
      # If this is a Contact_Meet_PlaceTime, it could also be a Contact_Meet_Entity
      if "Contact_Meet_PlaceTime" == ssf_line.split()[8] and\
        should_be_contact_entity(ssf_line, gpg_line, ex_line):

        new_ssf_line = transform_entity("Contact_Meet_Entity", ssf_line)

        print >> sys.stdout, new_ssf_line.strip()
        print >> sys.stdout, gpg_line.strip()
        print >> sys.stdout, ex_line.strip()
        change_entity += 1

      # enumerate founder of to generete founded by
      if "FounderOf" == ssf_line.split()[8].strip():
        new_ssf_line = transform_entity("FoundedBy", ssf_line)
        new_new_ssf_line = byte_range_correction(new_ssf_line, gpg_line, ex_line)

        print >> sys.stdout, new_new_ssf_line.strip()
        print >> sys.stdout, gpg_line.strip()
        print >> sys.stdout, ex_line.strip()
        change_entity += 1

      # For DateOfDeath Contact_Meet_Entity and Contact_Meet_PlaceTime compare on equal sentences
      if ssf_line.split()[8] in ("DateOfDeath", "Contact_Meet_Entity", "Contact_Meet_PlaceTime") and\
        ssf_line.split()[8] == old_ssf_line.split()[8] and\
        extract_sentence(ex_line) == extract_sentence(old_ex_line):
        dups += 1
        continue

      (old_ssf_line, old_gpg_line, old_ex_line) = (ssf_line, gpg_line, ex_line)
      print >> sys.stdout, ssf_line.strip()
      print >> sys.stdout, gpg_line.strip()
      print >> sys.stdout, ex_line.strip()

  return (total_items, dups, change_entity, too_long, anchor_distance)


def sentence_histogram(ssf_file):
  """ Extracts the sentence length from all the items ssf items and returns a histogram on the length.
  """
  with open(ssf_file, 'r') as f:

    c = Counter()
    file_iter = iter(f.readline, ' ')
    for line in file_iter:
      if line == '': break

      ssf_line = line
      gpg_line = file_iter.next()
      ex_line = file_iter.next()

      sentence = extract_sentence(ex_line)
      if sentence is not None:
        c.update([len(sentence)])

  sorted_list = sorted(c.items())
  for k,v in sorted_list:
    print >> sys.stdout, "%d %d"%(k,v)
 
 
def print_anchor_distance(ssf_file):
  """ Extracts the sentence length from all the items ssf items and returns a histogram on the length.
  """
  with open(ssf_file, 'r') as f:

    c = Counter()
    file_iter = iter(f.readline, ' ')
    for line in file_iter:
      if line == '': break

      ssf_line = line
      gpg_line = file_iter.next()
      ex_line = file_iter.next()

      dist = minimum_anchorword_distance(ssf_line, gpg_line, ex_line)
      if dist != 0:
        print >> sys.stdout, minimum_anchorword_distance(ssf_line, gpg_line, ex_line)
  

if __name__ == '__main__':
  usage = """
    Takes an SSF output file as input, and removes duplicate results.
    The output is printed to stdout. If the '-p' flag is used a histogram
    of the sentence sizes will be output.
    """

  parser = argparse.ArgumentParser(description=usage)
  parser.add_argument("--ssf_file", action='store', dest="ssf_file", required=True, type=str, help='The ssf_file')
  parser.add_argument("--print_histogram", '-p', default=False, action='store_true', help='If set it retuns a histogram a series of two numbers size and the frequency.') 
  parser.add_argument("--print_anchor_distance", '-a', default=False, action='store_true', help='Prints the distance between the entity and the slot values for all the entries.') 
  args = parser.parse_args()

  # Print the histogram only if the flag is present
  if args.print_histogram:
    sentence_histogram(args.ssf_file)
  elif args.print_anchor_distance:
    print_anchor_distance(args.ssf_file)
  else:
    (total_items, dups, change_entity, too_long, anchor_distance) = do_dedupe(args.ssf_file)
    print >> sys.stderr, "\nRead %d items, found %d duplicates, changed %d entities, long sentence %d and long anchor instances %d." % (total_items, dups, change_entity, too_long, anchor_distance)

