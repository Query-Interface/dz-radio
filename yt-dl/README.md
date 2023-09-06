# yt-dl
Use youtube-dl to download a video and convert it to an audio only file.

It consists of a java application with a Kafka consumer that leverage ytdlp (a youtube-dl fork) to download the videos from Youtube.
The Dockerfile is based on the following : https://github.com/mikenye/docker-youtube-dl 
