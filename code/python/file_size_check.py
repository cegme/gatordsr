#!/usr/bin/python

if __name__ == '__main__':
  usage = """
    This takes a file as input and prints the file size in bytes to stdout.

    Usage:
      >>> python file_size_check.py <filename>
      or 
      >>> ls . | python

    We can use this as part of a shell script that can compute the size of 
    all the files checked for entities.
    If we have a file <> with schema `<+/-> | <file name>' we can extract the
    file name and sum up the total size using the following command:

    > cat /media/sde/kba_file_index.txt | cut -d "|" -f 2 | python file_size_check.py | (tr '\n' +; echo 0) | bc
  """
  from os import stat
  import sys
  if len(sys.argv) > 1:
    print stat(sys.argv[-1]).st_size
  else:
    for file in sys.stdin.readlines():
        if len(file.strip()) == 0:
          print(usage)
        else:
          print stat(file.strip()).st_size
