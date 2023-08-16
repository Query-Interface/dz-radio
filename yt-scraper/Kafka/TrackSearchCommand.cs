namespace yt_scraper
{
    public class TrackSearchCommand : Message
    {
        
        public long PlaylistId { get; set; }
        public long TrackId { get; set; }
        public string Title { get; set; }
        public string Artist { get; set; }

        public string GetMessageType()
        {
            return "track.search.v1";
        }
    }
}