#!/usr/bin/python

import io
import re
import sys


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
  re.replace

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


def byte_range_correction(s,g,e):
  """ Takes an extraction and updates the byte range to include whole sentence.
      It returns the updates ssf_file.
      
  """
  # Extract the current byte range
  (br_s, br_e) = map(int, s.split()[-1].split("-"))

  # Extract the size of the sentence
  sentence = extract_sentence(e)
  sentence = remove_ner(sentence)

  (_,_,slot_value) = triple(e)
  
  # Find the last location location of the slot value
  idx = sentence.rfind(slot_value)

  br_s -= idx
  if br_s < 0:
    br_s = 0
  
  new_ssf = s.split()
  s[-1] = "%d=%d"%(br_s, br_e)

  return ' '.join(new_ssf)


def check_similar_byte_range(s1,g1,e1,s2,g2,e2):
  """ Compares the old and new byte range, if similar, and other fields are equal return true.

      We only do this if the other fields are equal.
  """
  if s1.split()[:-1] != s2.split()[:-1]:
    return false

  br1 = s1.split()[-1]
  br2 = s2.split()[-1]

  (br1_s, br1_e) = map(int, br1.split("-"))
  (br2_s, br2_e) = map(int, br2.split("-"))

  set1 = set(range(br1_s, br1_e))
  set2 = set(range(br2_s, br2_e))

  return len(set1 & set2) # If there is overlap then this is similar


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
  with open(ssf_file, 'r') as f:

    old_ssf_line = None
    old_gpg_line = None
    old_ex_line = None

    while (f.hasNext()):

      ssf_line = f.readline()
      gpg_line = f.readline()
      ex_line = f.readline()
      total_items += 1

      # If ssf_lines are the same
      if ssf_line == old_ssf_line:
        dups += 1
        continue

      # If it is Contact_Meet_PlaseTime, update the byte range
      if "Contact_Meet_PlaceTime" in ssf_line:
        ssf_line = byte_range_correction(ssf_line, gpg_line, old_ex_line)

      # If all is the same except for a small variation in the byte range
      if not check_similar_byte_range(ssf_file, gpg_line, ex_line, old_ssf_line,\
        old_gpg_line, old_ex_line) and ssf_file is not None:
        dups += 1
        continue
      
      
      (old_ssf_line, old_gpg_line, old_ex_line) = (ssf_file, gpg_line, ex_line)
      print >> sys.stdout, ssf_line
      print >> sys.stdout, gpg_line
      print >> sys.stdout, ex_line

  return (total_items, dups)

    

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
  (total_items, dups) = do_dedupe(ssf_file)

  print >> sys.stderr, "Read %d items and found %d duplicates." % (total_items, dups)
