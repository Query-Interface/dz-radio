using Microsoft.AspNetCore.Mvc;

namespace yt_scraper.Controllers
{
    [ApiController]
    [Route("[controller]")]
    public class HealthController : ControllerBase
    {
        private readonly ILogger<HealthController> _logger;

        public HealthController(ILogger<HealthController> logger)
        {
            _logger = logger;
        }

        [HttpGet(Name = "health")]
        public string Get()
        {
            return "alive";
        }
    }
}