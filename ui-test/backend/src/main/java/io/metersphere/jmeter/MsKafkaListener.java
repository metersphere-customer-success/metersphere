package io.metersphere.jmeter;


import io.metersphere.commons.constants.KafkaTopicConstants;
import io.metersphere.utils.JSONUtil;
import io.metersphere.utils.LoggerUtil;
import io.metersphere.websocket.c.to.c.WebSocketUtils;
import io.metersphere.websocket.c.to.c.util.MsgDto;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;


@Configuration
public class MsKafkaListener {
    public static final String DEBUG_CONSUME_ID = "ms-ui-debug-consume";


    @KafkaListener(id = DEBUG_CONSUME_ID, topics = KafkaTopicConstants.UI_DEBUG_TOPICS, groupId = "${spring.kafka.consumer.debug.group-id}")
    public void debugConsume(ConsumerRecord<?, String> record) {
        try {
            LoggerUtil.info("接收到执行结果：", record.key());
            if (ObjectUtils.isNotEmpty(record.value()) && WebSocketUtils.has(record.key().toString())) {
                MsgDto dto = JSONUtil.parseObject(record.value(), MsgDto.class);
                WebSocketUtils.sendMessageSingle(dto);
            }
        } catch (Exception e) {
            LoggerUtil.error("KAFKA消费失败：", e);
        }
    }
}
