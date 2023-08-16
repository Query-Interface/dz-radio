using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace yt_scraper
{
    public class SearchResult
    {
        public string Id {get; }
        public string Title { get; }

        public SearchResult(string id, string title)
        {
            this.Id = id;
            this.Title = title;
        }

        /// <summary>
        /// Calculate score in the range [0-100] to evaluate this search result
        /// </summary>
        /// <param name="artist">the artist we are looking for</param>
        /// <param name="title">the track title we are looking for</param>
        public void CalculateScore(string title, string artist)
        {
            int score = 0;
            string searchedArtist = artist.ToLowerInvariant();
            string searchedTitle = title.ToLowerInvariant();
            string searchResult = Title.ToLowerInvariant();
            if (searchResult.Contains(searchedTitle))
            {
                score += 40;
            }
            if (searchResult.Contains(searchedArtist))
            { 
                score += 20;
            }
            if (searchResult.Contains("official"))
            {
                score += 20;
            }
            if (searchResult.Contains("audio"))
            {
                score += 20;
            }
            this.Score = score;
        }

        public int Score { get; internal set; }
    }
}
