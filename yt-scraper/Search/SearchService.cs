using HtmlAgilityPack;
using Newtonsoft.Json;
using System.Text;

namespace yt_scraper.Search
{
    public class SearchService
    {
        private static readonly string BASE_URL = "https://www.youtube.com/results?search_query=";

        public List<SearchResult> searchVideos(TrackSearchCommand trackAddedEvent)
        {
            var data = this.Scrap(trackAddedEvent.Title, trackAddedEvent.Artist);
            List<SearchResult> results = this.Parse(data, new Tuple<string, string>(trackAddedEvent.Title, trackAddedEvent.Artist));
            results.Sort((r1, r2) => r2.Score - r1.Score);
            return results.Take(10).ToList();
        }

        private string Scrap(string title, string artist)
        {
            var web = new HtmlWeb();
            var document = web.Load(BASE_URL + SearchString(title, artist));
            var scripts = document.DocumentNode.SelectNodes("//body/script");
            string data = null;
            foreach (var script in scripts)
            {
                if (script.InnerText.StartsWith("var ytInitialData"))
                {
                    data = script.InnerText;
                    break;
                }
            }
            return data;
        }

        private string SearchString(string title, string artist)
        {
            StringBuilder builder = new StringBuilder();
            builder.Append(title.Replace(" ", "+"));
            builder.Append('+');
            builder.Append(artist.Replace(" ", "+"));
            return builder.ToString();
        }

        List<SearchResult> Parse(string data, Tuple<string, string> searchCriterias)
        {
            string content = data;
            var start = content.IndexOf("{");
            content = content.Substring(start);
            content = content.Substring(0, content.Length - 1);

            JsonTextReader reader = new JsonTextReader(new StringReader(content));
            List<SearchResult> results = new List<SearchResult>();
            while (reader.Read())
            {
                if (reader.TokenType == JsonToken.PropertyName && reader.Value.Equals("videoRenderer"))
                {
                    reader.Read();
                    int depth = reader.Depth;

                    string videoId = ReadVideoId(reader);
                    string title = ReadTitle(reader);
                    SearchResult result = new SearchResult(videoId, title);
                    result.CalculateScore(searchCriterias.Item1, searchCriterias.Item2);
                    if (result.Score > 50)
                    {
                        results.Add(result);
                    }
                }
            }
            return results;
        }

        void ReadToValue(JsonTextReader reader)
        {
            while (reader.TokenType != JsonToken.String)
            {
                reader.Read();
            }
        }

        void ReadToProperty(JsonTextReader reader, string propertyName)
        {
            while (reader.Read())
            {
                if (reader.TokenType == JsonToken.PropertyName && reader.Value.Equals(propertyName))
                {
                    break;
                }
            }
        }

        string ReadVideoId(JsonTextReader reader)
        {
            ReadToProperty(reader, "videoId");
            ReadToValue(reader);
            return reader.Value.ToString();
        }
        string ReadTitle(JsonTextReader reader)
        {
            ReadToProperty(reader, "text");
            ReadToValue(reader);
            return reader.Value.ToString();
        }
    }
}