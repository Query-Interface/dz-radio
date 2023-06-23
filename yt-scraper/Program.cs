// See https://aka.ms/new-console-template for more information
using HtmlAgilityPack;
using Newtonsoft.Json;
using yt_scraper;

Console.WriteLine("YT Parser");
/*var web = new HtmlWeb();
var document = web.Load("https://www.youtube.com/results?search_query=let+me+down+slowly");
var scripts = document.DocumentNode.SelectNodes("//body/script");
string data = null;
foreach (var script in scripts)
{
    if (script.InnerText.StartsWith("var ytInitialData"))
    {
        data = script.InnerText;
    }
    //Console.WriteLine(data);
    File.WriteAllText("data.txt", data);
}
*/
string content = File.ReadAllText("data.txt");
var start = content.IndexOf("{");
content = content.Substring(start);
content = content.Substring(0, content.Length - 1);

JsonTextReader reader = new JsonTextReader(new StringReader(content));
List<Video> videos = new List<Video>();
while (reader.Read())
{
    if (reader.TokenType == JsonToken.PropertyName && reader.Value.Equals("videoRenderer"))
    {
        reader.Read();
        int depth = reader.Depth;
        
        string videoId = ReadVideoId(reader);
        string title = ReadTitle(reader);
        videos.Add(new Video(videoId, title));
    }
}
foreach (Video v in videos)
{
    Console.WriteLine(v.Id + " : " + v.Title);
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