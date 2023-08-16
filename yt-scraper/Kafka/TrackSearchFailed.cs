namespace yt_scraper
{
    public class TrackSearchFailed : Message
    {
        public long PlaylistId { get; set; }
        public long TrackId { get; set; }
        public string Error { get; set; }

        public string GetMessageType()
        {
            return "track.search.failed.v1";
        }
    }
}