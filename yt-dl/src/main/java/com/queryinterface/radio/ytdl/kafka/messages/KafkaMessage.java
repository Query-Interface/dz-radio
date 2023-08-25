package com.queryinterface.radio.ytdl.kafka.messages;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface KafkaMessage {
    @JsonIgnore
    public String getType();
}
