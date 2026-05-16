package com.robot.grid.config;

import com.robot.grid.dto.Dtos.MoveMessage;
import com.robot.grid.dto.Dtos.PositionDto;
import com.robot.grid.dto.Dtos.ResultMessage;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

/**
 * Kafka topic declarations and per-topic typed listener container factories.
 *
 * Root cause of the LinkedHashMap error: our DTOs are nested records inside Dtos.java
 * (compiled as Dtos$MoveMessage etc.). A generic Object deserializer can't infer the
 * target type at runtime, so Jackson falls back to LinkedHashMap.
 *
 * Fix: give each @KafkaListener its own containerFactory with a JsonDeserializer
 * that is explicitly constructed with the correct target Class.
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")  private String bootstrapServers;
    @Value("${spring.kafka.consumer.group-id}")  private String groupId;
    @Value("${kafka.topics.move-instructions}")  private String moveInstructionsTopic;
    @Value("${kafka.topics.instruction-results}") private String instructionResultsTopic;
    @Value("${kafka.topics.golden-points}")       private String goldenPointsTopic;

    // ── Topic declarations ────────────────────────────────────────────────

    @Bean public NewTopic moveInstructionsTopic() {
        return TopicBuilder.name(moveInstructionsTopic).partitions(3).replicas(1).build();
    }

    @Bean public NewTopic instructionResultsTopic() {
        return TopicBuilder.name(instructionResultsTopic).partitions(3).replicas(1).build();
    }

    @Bean public NewTopic goldenPointsTopic() {
        return TopicBuilder.name(goldenPointsTopic).partitions(1).replicas(1).build();
    }

    // ── Typed listener container factories ───────────────────────────────
    // Each factory pins Jackson to the exact target type, avoiding the
    // LinkedHashMap → DTO conversion failure for nested record classes.

    /** Used by MoveConsumer — deserializes to MoveMessage */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, MoveMessage>
    moveListenerContainerFactory() {
        return buildFactory(MoveMessage.class);
    }

    /** Used by ResultConsumer — deserializes to ResultMessage */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ResultMessage>
    resultListenerContainerFactory() {
        return buildFactory(ResultMessage.class);
    }

    /** Used by GoldenPointConsumer — deserializes to PositionDto */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PositionDto>
    goldenPointListenerContainerFactory() {
        return buildFactory(PositionDto.class);
    }

    // ── Shared builder ────────────────────────────────────────────────────

    private <T> ConcurrentKafkaListenerContainerFactory<String, T> buildFactory(Class<T> targetType) {
        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType);
        deserializer.addTrustedPackages("com.robot.grid.dto");
        deserializer.setUseTypeHeaders(false);

        ConsumerFactory<String, T> factory = new DefaultKafkaConsumerFactory<>(
            Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG,          groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
            ),
            new StringDeserializer(),
            deserializer
        );

        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, T>();
        containerFactory.setConsumerFactory(factory);
        return containerFactory;
    }
}
