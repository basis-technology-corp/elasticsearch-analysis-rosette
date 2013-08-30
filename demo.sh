function reportSuccess {
	 RETVAL=$?
	 if [ $RETVAL -eq 0 ]; 
	 then 
	      echo success
	 else
	      echo failure
	 fi
}
echo
echo Testing English documents and queries:
echo
echo 1. Deleting the \'rosette-test-eng\' index which may exist from a previous run of this script...
curl -XDELETE 'http://localhost:9200/rosette-test-eng' && echo
echo
echo 2. Instantiating a \'rosette-test-eng\' index and associating an English Rosette Analyzer...
curl -XPUT 'http://localhost:9200/rosette-test-eng' -d '{
     "settings": {
         "analysis": {
             "analyzer": {
                 "rosetteAnalyzerEng": {
                     "type": "rosette",
                     "bt.lang": "eng"
                 }
             }
         }
     }
}' && echo
echo
echo 3. Specifying the \'typeEnglish\' doc type and that we want the English analyzer to parse the \'body\' text...
curl -XPUT 'http://localhost:9200/rosette-test-eng/typeEnglish/_mapping' -d '{
    "typeEnglish": {
        "properties" : {
            "body" : { "type" : "string", "analyzer" : "rosetteAnalyzerEng" }
        }
    }
}' && echo
echo
echo 4. Adding four English documents to the \'rosette-test-eng\' index...
curl -XPUT 'http://localhost:9200/rosette-test-eng/typeEnglish/1' -d '{"body": "The quick brown fox jumps over the lazy dog."}' && echo
curl -XPUT 'http://localhost:9200/rosette-test-eng/typeEnglish/2' -d '{"body": "I painted the barn brown."}' && echo
curl -XPUT 'http://localhost:9200/rosette-test-eng/typeEnglish/3' -d '{"body": "The celebrities eagerly anticipated the Oscars."}' && echo
curl -XPUT 'http://localhost:9200/rosette-test-eng/typeEnglish/4' -d '{"body": "The celebrations are scheduled for next month."}' && echo
echo
echo 5. Refreshing the index so the newly indexed documents are available for queries...
curl -XPOST 'http://localhost:9200/rosette-test-eng/_refresh' && echo
echo
echo 6. Running four queries and grepping for the ids of the documents found for each...
echo
echo "Should return 1:"
curl -s 'http://localhost:9200/rosette-test-eng/typeEnglish/_search?pretty=true' -d '{"query": {"query_string": {"query": "body:lazy"}}, "fields":["_id"]}'  | grep "_id" | tr '\n' ' ' | grep "id.\+1"
reportSuccess
echo
echo "Should return 1, 2:"
curl -s 'http://localhost:9200/rosette-test-eng/typeEnglish/_search?pretty=true' -d '{"query": {"query_string": {"query": "body:brown"}}, "fields":["_id"]}'  | grep "_id" | tr '\n' ' ' | grep "id.\+2.\+id.\+1"
reportSuccess
echo
echo "Should return 1:"
curl -s 'http://localhost:9200/rosette-test-eng/typeEnglish/_search?pretty=true' -d '{"query": {"query_string": {"query": "body:jump"}}, "fields":["_id"]}'  | grep "_id" | tr '\n' ' ' | grep "id.\+1"
reportSuccess
echo
echo "Should return 3:"
curl -s 'http://localhost:9200/rosette-test-eng/typeEnglish/_search?pretty=true' -d '{"query": {"query_string": {"query": "body:celebrity"}}, "fields":["_id"]}'  | grep "_id" | tr '\n' ' ' | grep "id.\+3"
reportSuccess

echo
echo Testing Japanese documents and queries:
echo
echo 1. Deleting the \'rosette-test-jpn\' index  which may exist from a previous run of this script...
curl -XDELETE 'http://localhost:9200/rosette-test-jpn' && echo
echo
echo 2. Instantiating the \'rosette-test-jpn\' index and associating a Japanese Rosette Analyzer...
curl -XPUT 'http://localhost:9200/rosette-test-jpn' -d '{
     "settings": {
         "analysis": {
             "analyzer": {
                 "rosetteAnalyzerJpn": {
                     "type": "rosette",
                     "bt.lang": "jpn"
                 }
             }
         }
     }
}' && echo
echo
echo 3. Specifying the \'typeJapanese\' doc type and that we want the Japanese analyzer to parse the \'body\' text...
curl -XPUT 'http://localhost:9200/rosette-test-jpn/typeJapanese/_mapping' -d '{
    "typeJapanese": {
        "properties" : {
            "body" : { "type" : "string", "analyzer" : "rosetteAnalyzerJpn" }
        }
    }
}' && echo

echo
echo 4. Adding four Japanese documents to the \'rosette-test-jpn\' index...
curl -XPUT 'http://localhost:9200/rosette-test-jpn/typeJapanese/1' -d '{"body": "T水泳の世界選手権第１１日は２７日、豪州・メルボルンで行われ、女子百メートル背泳ぎ決勝で中村礼子（東京ＳＣ）が１分０秒４０の日本人として大会初の銅メダルを獲得した。"}' && echo
curl -XPUT 'http://localhost:9200/rosette-test-jpn/typeJapanese/2' -d '{"body": "優勝は５９秒４４の世界新をマークしたアメリカ人のナタリー・コーグリン。"}' && echo
curl -XPUT 'http://localhost:9200/rosette-test-jpn/typeJapanese/3' -d '{"body": "女子千五百メートル自由形決勝では、柴田亜衣（チームアリーナ）が１５分５８秒５５をマークし、２日続けて同種目の日本記録を更新し銅メダル。"}' && echo
curl -XPUT 'http://localhost:9200/rosette-test-jpn/typeJapanese/4' -d '{"body": "男子百メートル背泳ぎ決勝は、アーロン・ピアソル（米）が５２秒９８の世界新で優勝し、森田智己（セントラルスポーツ）は８位。"}' && echo
curl -XPOST 'http://localhost:9200/rosette-test-jpn/_refresh' && echo
echo
echo 5. Refreshing the index so the newly indexed documents are available for queries...
curl -XPOST 'http://localhost:9200/rosette-test-jpn/_refresh' && echo
echo
echo 6. Running four Japanese queries and grepping for the ids of the documents found for each...
echo
echo "Should return 1:"
curl -s 'http://localhost:9200/rosette-test-jpn/typeJapanese/_search?pretty=true' -d '{"query": {"query_string": {"query": "body:豪州"}}, "fields":[ "_id"]}'   | grep "_id" | tr '\n' ' ' | grep "id.\+1"
reportSuccess
echo
echo "Should return 2:"
curl -s 'http://localhost:9200/rosette-test-jpn/typeJapanese/_search?pretty=true' -d '{"query": {"query_string": {"query": "body:コーグリン"}}, "fields":["_id"]}'   | grep "_id" | tr '\n' ' ' | grep "id.\+2"
reportSuccess
echo
echo "Should return 3:"
curl -s 'http://localhost:9200/rosette-test-jpn/typeJapanese/_search?pretty=true' -d '{"query": {"query_string": {"query": "body:柴田亜衣"}}, "fields":["_id"]}'   | grep "_id" | tr '\n' ' ' | grep "id.\+3"
reportSuccess
echo
echo "Should return 4:"
curl -s 'http://localhost:9200/rosette-test-jpn/typeJapanese/_search?pretty=true' -d '{"query": {"query_string": {"query": "body:アーロン"}}, "fields":["_id"]}'   | grep "_id" | tr '\n' ' ' | grep "id.\+4"
reportSuccess
echo
echo Done.