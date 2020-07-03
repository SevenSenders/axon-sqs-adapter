package com.sevensenders.axon.jms.config;

import com.sevensenders.axon.jms.JMSProperties;
import com.sevensenders.axon.jms.config.eventhandling.JMSMessagePublisher;
import com.sevensenders.axon.jms.message.AxonMessageConverter;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.axonframework.springboot.autoconfig.InfraConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.core.JmsTemplate;

@Configuration
@EnableConfigurationProperties(JMSProperties.class)
@AutoConfigureAfter({JmsAutoConfiguration.class, AxonAutoConfiguration.class, InfraConfiguration.class})
public class JMSAutoConfiguration {

    private final JMSProperties jmsProperties;

    public JMSAutoConfiguration(JMSProperties jmsProperties) {
        this.jmsProperties = jmsProperties;
    }

    @ConditionalOnProperty("axon.jms.event-queue")
    @ConditionalOnBean(JmsTemplate.class)
    @Bean(initMethod = "start", destroyMethod = "shutDown")
    public JMSMessagePublisher messagePublisher(EventBus eventBus, JmsTemplate jmsTemplate, AxonMessageConverter axonMessageConverter) {
        JMSMessagePublisher JMSMessagePublisher = new JMSMessagePublisher(eventBus, jmsTemplate, axonMessageConverter);
        JMSMessagePublisher.setQueueName(jmsProperties.getEventQueue());
        return JMSMessagePublisher;
    }

    @ConditionalOnMissingBean
    @Bean
    public AxonMessageConverter axonMessageConverter(@Qualifier("eventSerializer") Serializer eventSerializer) {
        return new AxonMessageConverter(eventSerializer);
    }

}
