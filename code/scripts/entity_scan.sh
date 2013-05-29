
#ls -lrt -d -1 $PWD/*.gpg | head
#ls -lU -d -1 /media/sd{d,e}/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/????-??-??-??/*.gpg | head
#for gpg_file in /media/sd{d,e}/s3.amazonaws.com/aws-publicdatasets/trec/kba/kba-streamcorpus-2013-v0_2_0-english-and-unknown-language/????-??-??-??/*.gpg do
#  echo ls
#done

find /media/sd{d,e}/s3.amazonaws.com/ -name '*.gpg' | head | \
  ./parallel --eta ../cpp/main #| gpg --no-permission-warning --trust-model always --output - --decrypt - | xz -- decompress


