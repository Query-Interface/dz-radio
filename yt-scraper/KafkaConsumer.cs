using Confluent.Kafka;
using Microsoft.AspNetCore.DataProtection.KeyManagement;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;
using Newtonsoft.Json.Serialization;
using yt_scraper.Controllers;
using yt_scraper.Search;
using static Confluent.Kafka.ConfigPropertyNames;

namespace yt_scraper
{
    public class KafkaConsumer : IHostedService
    {
        private readonly string groupId = "tracks_handlers";
        private readonly string bootstrapServers = "localhost:29092";
        private readonly static string MESSAGE_TYPE = "type";
        private readonly static string TRACK_SEARCH_COMMAND = "track.search.v1";
        private readonly static string TRACK_SEARCH_SUCCEEDED = "track.search.succeeded.v1";
        private readonly static string TRACK_SEARCH_FAILED = "track.search.failed.v1";
        private SearchService searchService = new SearchService();
        private readonly ILogger<KafkaConsumer> _logger;

        public Task StartAsync(CancellationToken cancellationToken)
        {
            var consumerConfig = new ConsumerConfig
            {
                GroupId = groupId,
                BootstrapServers = bootstrapServers,
                AutoOffsetReset = AutoOffsetReset.Earliest
            };
            using (var consumer = new ConsumerBuilder<string, string>(consumerConfig).Build())
            {
                consumer.Subscribe(Topics.DZ_PLAYLIST_COMMANDS);
                var cancelToken = new CancellationTokenSource();
                try
                {
                    while (true)
                    {
                        cancellationToken.ThrowIfCancellationRequested();

                        var consumerResult = consumer.Consume(cancelToken.Token);
                        var messageKey = consumerResult.Key;
                        var messageType = System.Text.Encoding.UTF8.GetString(consumerResult.Message.Headers.GetLastBytes(MESSAGE_TYPE));
                        if (messageType.Equals(TRACK_SEARCH_COMMAND))
                        {
                            this.HandleTrackAddedEvent(messageKey, JsonConvert.DeserializeObject<TrackSearchCommand>(consumerResult.Message.Value));
                        }
                        else
                        {
                            _logger.LogError($"Invalid message type: {messageKey}");
                        }
                    }
                }
                catch (Exception e)
                {
                    _logger.LogError(e.Message);
                    consumer.Close();
                }
            }
            return Task.CompletedTask;
        }

        private void HandleTrackAddedEvent(string messageKey, TrackSearchCommand addEvent)
        {
            Console.WriteLine($"Track {addEvent.Title} ({addEvent.TrackId}) by {addEvent.Artist} added to playlist {addEvent.PlaylistId}.");
            List<SearchResult> results = this.searchService.searchVideos(addEvent);
            TrackSearchSucceeded response = new TrackSearchSucceeded
            {
                PlaylistId = addEvent.PlaylistId,
                TrackId = addEvent.TrackId,
                Results = results
            };
            this.PublishMessage(messageKey, response);
        }

        public Task StopAsync(CancellationToken cancellationToken)
        {
            return Task.CompletedTask;
        }

        private void PublishMessage(string messageKey, Message message)
        {
            var producerConfig = new ProducerConfig
            {
                BootstrapServers = bootstrapServers,
                AllowAutoCreateTopics = true,
            };
            using (var producer = new ProducerBuilder<string, string>(producerConfig).Build())
            {
                try
                {
                    Message<string, string> kafkaMessage = new Message<string, string>();
                    Headers headers = new Headers();
                    headers.Add("type", System.Text.Encoding.UTF8.GetBytes(message.GetMessageType()));
                    kafkaMessage.Headers = headers;
                    kafkaMessage.Key = messageKey;

                    DefaultContractResolver contractResolver = new DefaultContractResolver
                    {
                        NamingStrategy = new CamelCaseNamingStrategy
                        {
                            OverrideSpecifiedNames = false
                        }
                    };

                    kafkaMessage.Value = JsonConvert.SerializeObject(message, new JsonSerializerSettings
                    {
                        ContractResolver = contractResolver,
                        Formatting = Formatting.Indented
                    });
                    producer.Produce(Topics.DZ_PLAYLIST_RESPONSES, kafkaMessage);
                    producer.Flush();
                }
                catch (Exception e)
                {
                    _logger.LogError($"Failed to deliver message: {e.Message}");
                }
            }
        }
    }
}
