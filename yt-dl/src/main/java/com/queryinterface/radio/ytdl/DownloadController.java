package com.queryinterface.radio.ytdl;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.queryinterface.radio.ytdl.services.YtDownloadService;
import com.queryinterface.radio.ytdl.kafka.messages.TrackDownloadCommand;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class DownloadController {
    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    private YtDownloadService downloadService;

    public DownloadController(YtDownloadService ytdlService) {
        this.downloadService = ytdlService;
    }

    @PostMapping(path = "api/test/tracks/")
    public void newDownload(TrackDownloadCommand dlCommand) {
        downloadService.download(dlCommand);
    }

}
