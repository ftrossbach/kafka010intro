package de.ftrossbach.streams.shout;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;


import java.util.Properties;

public class Shout {

    public static void main(String[] args) {

        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "shout2");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        streamsConfiguration.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "localhost:2181");
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put("auto.offset.reset", "earliest");


        final Serde<String> stringSerde = Serdes.String();

        KStreamBuilder builder = new KStreamBuilder();
        //lese die Key-Value Paare aus dem Topic faust
        KStream<String, String> textLines = builder.stream(stringSerde, stringSerde, "faust");
        //transformiere die Paare, in dem der Value (line) in Großbuchstaben formatiert wird
        KStream<String, String> upperCaseLines = textLines.map((key, line) -> new KeyValue<>(key, line.toUpperCase()));
        //schreibe den transformierten Stream in das Topic faust-shout
        upperCaseLines.to(stringSerde, stringSerde, "faust-shout");


        KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
        //hier startet die Transformations-Pipeline
        streams.start();
    }
}
