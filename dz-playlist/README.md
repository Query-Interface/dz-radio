# dz-playlist
Use the Deezer API to get tracks from a public playlist

# TODOs

 - today, kafka listerner must be configured in KafkaMessageHandler and in Kafka config; the first configure method handlers based topics/message type header, the other one configure deserialization based on message.type
   possible solutions:
     - add a builder to configure kafka listener and deserialization (can be done as a Bean ?)
     - add annotations to configure by reflection
