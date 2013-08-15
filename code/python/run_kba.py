#!/usr/bin/python

import shlex
import subprocess
import sys


# Function to run the CCR
def ccr(infile, outfile):
  # TODO code to run the code
  # subprocess info http://docs.python.org/2/library/subprocess.html#subprocess.call
  cmd = shlex.split("sbt 'run-main edu.ufl.cise.kb.WikiAPI' > %s " % outfile)
  print >> sys.stderr, "Running... %s" % ' '.join(cmd)
  subprocess.call(cmd)
  pass


# Function to run the ER
def er(infile, outfile):
  # TODO code to run the code
  cmd = shlex.split("echo 'run er thing'")
  print >> sys.stderr, "Running... %s" % ' '.join(cmd)
  subprocess.call(cmd)
  pass


# Function to run the SSF
def ssf(infile, outfile):
  # TODO code to run the code
  cmd = shlex.split("echo 'run ssf thing'")
  print >> sys.stderr, "Running... %s" % ' '.join(cmd)
  subprocess.call(cmd)
  pass


if __name__ == "__main__":
  import argparse

  parser = argparse.ArgumentParser(description='This is the file to execute the KBA program for the Gator DSR team')
  parser.add_argument("--ccrout", metavar="ccr_file", type=str, default="/tmp/ccr", nargs=1, help="This is the output file for the ccr step")
  parser.add_argument("--erout", metavar="cr_file", type=str, default="/tmp/ssf", nargs=1, help="This is the output file for the er step")
  parser.add_argument("--ssfout", metavar="ssf_file", type=str, default="/tmp/ssf", nargs=1, help="This is the output file for the ssf step")

  args = parser.parse_args()

  # TODO do we need to compile everything?

  # Run the pipeline
  ccr("", args.ccrout)
  er(args.ccrout, args.erout)
  ssf(args.erout, args.ssfout)

