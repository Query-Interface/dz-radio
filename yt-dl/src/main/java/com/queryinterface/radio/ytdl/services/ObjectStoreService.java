package com.queryinterface.radio.ytdl.services;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.queryinterface.radio.ytdl.kafka.messages.TrackDownloadCommand;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import okio.Path;

@Service
public class ObjectStoreService {
    private static String youtubeVideoUrl = "https://www.youtube.com/watch?v=";
    private static String dlPath = "/home/dockeruser/videos";
    private static Logger logger = LoggerFactory.getLogger(YtDownloadService.class);

    public boolean uploadToMinio(TrackDownloadCommand dlCommand) {
        boolean success = true;
        try {
            logger.info("Starting upload to ObjectStore of video: {} for playlist: {}", dlCommand.videoId(), dlCommand.playlistId());
            MinioClient minioClient =
                MinioClient.builder()
                    .endpoint("http://objectstore:9000")
                    .credentials("cKodLANWsGcQboYxAh2O", "dxzLWZiKqIyqWxfSmo3sD7bHRcwQFNSfNRMlE8xM")
                    .build();

            String playlistBucket = getBucketName(dlCommand);
            boolean playListBucketFound = minioClient.bucketExists(BucketExistsArgs.builder().bucket(playlistBucket).build());
            if (!playListBucketFound) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(playlistBucket).build());
            }

            minioClient.uploadObject(
                UploadObjectArgs.builder()
                    .bucket(playlistBucket)
                    .object(getObjectName(dlCommand))
                    .filename(getFilePath(dlCommand.videoId()))
                    .build());
            logger.info("Uploaded video {} as track {} for playlist {}", youtubeVideoUrl+dlCommand.videoId(), dlCommand.trackId(), dlCommand.playlistId());
        } catch (MinioException e) {
            System.out.println("Error occurred: " + e);
            System.out.println("HTTP trace: " + e.httpTrace());
            success = false;
        } catch (Exception e) {
            System.out.println("Error occurred: " + e);
            success = false;
        }
        return success;
    }

    public static String getBucketName(TrackDownloadCommand dlCommand) {
        return String.valueOf(dlCommand.playlistId());
    }

    public static String getObjectName(TrackDownloadCommand dlCommand) {
        return String.valueOf(dlCommand.trackId()) + Path.DIRECTORY_SEPARATOR + dlCommand.videoId() + ".mp3";
    }

    private String getFilePath(String videoId) {
        return dlPath + "/" + videoId + ".mp3";
    }
}
