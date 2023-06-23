using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace yt_scraper
{
    internal class Video
    {
        public string Id
        {
            get;
            set;
        }
        public string Title { get; set; }

        public Video(string id, string title)
        {
            this.Id = id;
            this.Title = title;
        }
    }
}
