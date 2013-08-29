# Run this from inside of the gatordsr/code/scripts/ folder

# Export file so the cpp file can read the libthrift-0.9.0.so
export LD_LIBRARY_PATH=/usr/local/lib

# Run it!
#time find /media/sd{d,e}/s3.amazonaws.com/ -name '*.gpg'\
#time ls /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-08-18-01/*.gpg\
#time ls /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-08-18-??/*.gpg\

# Example run with one file
# gpg --quiet --no-permission-warning --trust-model always --decrypt /media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2013-02-13-23/arxiv-12-e487252b3b485ec496a12694a84a2a80-8a4272a7c577f4344508eea6628e010e.sc.xz.gpg | xz --decompress --stdout | ./entity_match /media/sde/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2013-02-13-23/arxiv-12-e487252b3b485ec496a12694a84a2a80-8a4272a7c577f4344508eea6628e010e.sc.xz.gpg

time find /media/sd{d,e}/s3.amazonaws.com/ -name '*.gpg'\
  | parallel -u -j+0 --progress "gpg --quiet --no-permission-warning --trust-model always --decrypt {} | xz --decompress --stdout | ../cpp/entity_match '{}'"


