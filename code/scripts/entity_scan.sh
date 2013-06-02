# Run this from inside of the gatordsr/code/scripts/ folder

# Export file so the cpp file can read the libthrift-0.9.0.so
export LD_LIBRARY_PATH=/usr/local/lib

# Run it!
#time find /media/sd{d,e}/s3.amazonaws.com/ -name '*.gpg'\
#time ls /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-08-18-01/*.gpg\
#time ls /media/sdd/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/2012-08-18-??/*.gpg\
time find /media/sd{d,e}/s3.amazonaws.com/ -name '*.gpg'\
  | parallel -u -j+0 --progress "gpg --quiet --no-permission-warning --trust-model always --decrypt {} | xz --decompress --stdout | ../cpp/main '{}'"


