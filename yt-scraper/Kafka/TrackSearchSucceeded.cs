namespace yt_scraper
{
    public class TrackSearchSucceeded : Message
    {
        public long PlaylistId { get; set; }
        public long TrackId { get; set; }
        public List<SearchResult> Results { get; set; }
        public string GetMessageType()
        {
            return "track.search.succeeded.v1";
        }
    }
}