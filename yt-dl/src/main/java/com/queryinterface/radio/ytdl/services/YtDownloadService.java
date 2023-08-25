package com.queryinterface.radio.ytdl.services;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.queryinterface.radio.ytdl.dto.DownloadDto;
import com.queryinterface.radio.ytdl.kafka.Topics;
import com.queryinterface.radio.ytdl.kafka.messages.KafkaMessage;
import com.queryinterface.radio.ytdl.kafka.messages.TrackDownloadCommand;
import com.queryinterface.radio.ytdl.kafka.messages.TrackDownloadFailed;
import com.queryinterface.radio.ytdl.kafka.messages.TrackDownloadSucceeded;




@Service
public class YtDownloadService {

    private static String ytdlExecutablePath = "/usr/local/bin/yt-dlp";
    private static String youtubeVideoUrl = "https://www.youtube.com/watch?v=";
    private static String dlPath = "/home/dockeruser/videos";

    private KafkaPublisherService publisherService;
    private ObjectStoreService objectStoreService;

    private static Logger logger = LoggerFactory.getLogger(YtDownloadService.class);

    public YtDownloadService(KafkaPublisherService publisherService, ObjectStoreService objectStoreService) {
        this.publisherService = publisherService;
        this.objectStoreService = objectStoreService;
    }

    @Async("downloadExecutor")
    public CompletableFuture<DownloadDto> download(TrackDownloadCommand dlCommand) {
        CompletableFuture<DownloadDto> future = new CompletableFuture<>();
        String url = youtubeVideoUrl + dlCommand.videoId();
        KafkaMessage message = new TrackDownloadFailed(dlCommand.playlistId(), dlCommand.trackId());
        DownloadDto dlResult = new DownloadDto(dlCommand.playlistId(), dlCommand.trackId(), false, null, null);

        try {
            logger.info("Starting download of video: {} for playlist: {}", dlCommand.videoId(), dlCommand.playlistId());
            ProcessBuilder processBuilder = new ProcessBuilder(ytdlExecutablePath,
                                                            "-P",
                                                            dlPath,
                                                            "--extract-audio", // post-processing, extract audio
                                                            "--audio-format",
                                                            "mp3", // audio format
                                                            "--id", // use only id in the filename
                                                            url);
            processBuilder.redirectOutput();
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor();

        } catch (IOException e) {
            logger.error("Download failed", e);
        } catch (InterruptedException e) {
            logger.error("Download failed", e);
        }
        if (isTrackDownloaded(dlCommand.videoId())) {
            logger.info("track " + dlCommand.videoId() + " downloaded");
            if (objectStoreService.uploadToMinio(dlCommand)) {
                message = new TrackDownloadSucceeded(dlCommand.playlistId(), dlCommand.trackId(), ObjectStoreService.getBucketName(dlCommand),ObjectStoreService.getObjectName(dlCommand));
                dlResult = new DownloadDto(dlCommand.playlistId(), dlCommand.trackId(), true, ObjectStoreService.getBucketName(dlCommand),ObjectStoreService.getObjectName(dlCommand));
            } else {
                logger.info("Failed to upload file to ObjectStore {}:{}/{}", dlCommand.playlistId(), dlCommand.trackId(), dlCommand.videoId());
            }
        } else {
            logger.info("Download failed");
        }

        // in all case, send Kafka message in response topic
        this.publisherService.publishMessage(Topics.DZ_PLAYLIST_RESPONSES, message);

        future.complete(dlResult);
        return future;
    }

    private boolean isTrackDownloaded(String videoId) {
        Path path = Paths.get(getFilePath(videoId));
        return Files.exists(path);
    }

    private String getFilePath(String videoId) {
        return dlPath + "/" + videoId + ".mp3";
    }

}
