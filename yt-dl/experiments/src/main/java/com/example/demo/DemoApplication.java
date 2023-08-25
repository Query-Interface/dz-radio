package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	public static String ytdl = "/usr/local/bin/yt-dlp";
  	private static Logger LOG = LoggerFactory
      .getLogger(DemoApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("Yes, we are running !");
		LOG.info("arg:" + args[0]);
		String url = args[0]; 
		
		ProcessBuilder processBuilder = new ProcessBuilder(ytdl, " --extract-audio --audio-format mp3 https://www.youtube.com/watch?v=A2VpR8HahKc");
		processBuilder.redirectOutput();
		processBuilder.redirectErrorStream(true);
//		Process process = processBuilder.start();

		Process process = Runtime.getRuntime().exec(ytdl + " -P /home/dockeruser/videos --extract-audio --audio-format mp3 " + url);
		process.waitFor();
		System.out.println(process.exitValue());
		process.getInputStream();
		process.errorReader().lines().forEach(System.out::println);
		LOG.info("downloaded !!!!");

		//Thread.sleep(30*1000);
	}

}
