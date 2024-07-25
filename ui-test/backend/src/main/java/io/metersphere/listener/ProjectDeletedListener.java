package io.metersphere.listener;

import io.metersphere.commons.constants.KafkaTopicConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProjectDeletedListener {
    public static final String CONSUME_ID = "project-deleted";

    @KafkaListener(id = CONSUME_ID, topics = KafkaTopicConstants.PROJECT_DELETED_TOPIC, groupId = "${spring.application.name}")
    public void consume(ConsumerRecord<?, String> record) {
        String projectId = record.value();
        System.out.println(projectId);
        // todo

    }

}
