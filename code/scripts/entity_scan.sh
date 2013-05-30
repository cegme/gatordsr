
#ls -lrt -d -1 $PWD/*.gpg | head
#ls -lU -d -1 /media/sd{d,e}/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/????-??-??-??/*.gpg | head
#for gpg_file in /media/sd{d,e}/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/????-??-??-??/*.gpg do
#  echo ls
#done

# Export file so the cpp file can read the libthrift-0.9.0.so
export LD_LIBRARY_PATH=/usr/local/lib

# Run it!
time find /media/sd{d,e}/s3.amazonaws.com/ -name '*.gpg' | head -n1000\
  | parallel -j 128 --eta "gpg --quiet --no-permission-warning --trust-model always --decrypt {} | xz --decompress --stdout | ../cpp/main '{}'"
  #| parallel --eta "xz --decompress < gpg --no-permission-warning --trust-model always --decrypt {} | ../cpp/main '{}'"


