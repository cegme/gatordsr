#!/usr/bin/python

import subprocess


# Function to run the CCR
def ccr(infile, outfile):
  # TODO code to run the code
  # subprocess info http://docs.python.org/2/library/subprocess.html#subprocess.call
  subprocess.call(["echo", "'sbt run-main ...'"])
  pass


# Function to run the ER
def er(infile, outfile):
  # TODO code to run the code
  print infile
  subprocess.call(["echo", "'./er'", "infile", "outfile"])
  pass


# Function to run the SSF
def ssf(infile, outfile):
  # TODO code to run the code
  subprocess.call(["echo", "'sbt run-main ...'"])
  pass


if __name__ == "__main__":
  import argparse

  parser = argparse.ArgumentParser(description='This is the file to execute the KBA program for the Gator DSR team')
  parser.add_argument("ccrout", metavar="ccr_file", type=str, default="/tmp/ccr", nargs=1, help="This is the output file for the ccr step")
  parser.add_argument("erout", metavar="cr_file", type=str, default="/tmp/ssf", nargs=1, help="This is the output file for the er step")
  parser.add_argument("ssfout", metavar="ssf_file", type=str, default="/tmp/ssf", nargs=1, help="This is the output file for the ssf step")

  args = parser.parse_args()

  # TODO do we need to compile everything?

  # Run the pipeline
  ccr("", args.ccrout)
  er(args.ccrout, args.erout)
  ssf(args.erout, args.ssfout)

