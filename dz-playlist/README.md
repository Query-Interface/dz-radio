# dz-playlist
Use the Deezer API to get tracks from a public playlist
Store tracks in its own database.
Is able to detect new tracks.
When new track are identified, it creates a Kafka command to request the Search of this track in Youtube.
Then it basically behaves has a saga orchestrator that follows the evolution of the process by consumming the events/commands send on Kafka.

# TODOs

 - today, kafka listerner must be configured in KafkaMessageHandler and in Kafka config; the first configure method handlers based topics/message type header, the other one configure deserialization based on message.type
   possible solutions:
     - add a builder to configure kafka listener and deserialization (can be done as a Bean ?)
     - add annotations to configure by reflection
