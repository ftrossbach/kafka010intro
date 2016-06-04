package de.ftrossbach.streams.count;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.kstream.KTable;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Count {
    final static Pattern pattern = Pattern.compile("([A-Z]{2,}\\s?([A-Z]*)).*:");
    public static void main(String[] args) {

        Properties streamsConfiguration = new Properties();
        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, "count");
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        streamsConfiguration.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "localhost:2181");
        streamsConfiguration.put(StreamsConfig.KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put("auto.offset.reset", "earliest");
        streamsConfiguration.put("interceptor.classes","io.confluent.monitoring.clients.interceptor.MonitoringConsumerInterceptor");
        streamsConfiguration.put("interceptor.classes","io.confluent.monitoring.clients.interceptor.MonitoringProducerInterceptor");

        final Serde<String> stringSerde = Serdes.String();
        final Serde<Long> longSerde = Serdes.Long();

        KStreamBuilder builder = new KStreamBuilder();
        //lese die Key-Value Paare aus dem Topic faust
        KStream<String, String> textLines = builder.stream(stringSerde, stringSerde, "faust");


        //filtere nur die Zeilen heraus, die dem Schema "NAME IN GROSSBUCHSTABEN:" folgen
        KStream<String, String> filteredLines = textLines.filter((key, value) -> pattern.matcher(value).matches());

        //extrahiere den Characternamen und schreibe ihn als Schlüssel
        KStream<String, String> characterNameAsKey = filteredLines.map((key, value) -> {
            Matcher matcher = pattern.matcher(value);
            matcher.find();
            return new KeyValue<>(matcher.group(1).trim(), value);
        });

        //Zähle die Vorkommen der Schlüssel (Charakternamen)
        KTable<String, Long> countTable = characterNameAsKey.countByKey("CountTable");

        //Kombiniere Charaktername (key) und Wert im Wert
        KStream<String, String> countStream = countTable.toStream().map((key,value) -> new KeyValue<>(key, key + ": " + value));


        //schreibe den transformierten Stream in das Topic faust-count
        countStream.to(stringSerde, stringSerde, "faust-count");


        KafkaStreams streams = new KafkaStreams(builder, streamsConfiguration);
        //hier startet die Transformations-Pipeline
        streams.start();
    }
}
