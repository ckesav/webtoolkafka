package org.example.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.hubspot.jackson.datatype.protobuf.ProtobufModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.manager.SensitiveConfigScrubber;
import org.example.manager.encryption.SecretManager;
import org.example.manager.kafka.KafkaAdminFactory;
import org.example.manager.kafka.KafkaClientConfigUtil;
import org.example.manager.kafka.KafkaConsumerFactory;
import org.example.manager.kafka.KafkaOperationsFactory;
import org.example.manager.kafka.WebKafkaConsumerFactory;
import org.example.manager.plugin.PluginFactory;
import org.example.manager.plugin.UploadManager;
import org.example.manager.sasl.SaslUtility;
import org.example.plugin.filter.RecordFilter;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Application Configuration for Plugin beans.
 */
@Component
public class PluginConfig {
    private static final Logger logger = LoggerFactory.getLogger(PluginConfig.class);

    /**
     * Upload manager, for handling uploads of Plugins and Keystores.
     * @param appProperties Definition of app properties.
     * @return UploadManager for Plugins
     */
    @Bean
    public UploadManager getPluginUploadManager(final AppProperties appProperties) {
        return new UploadManager(appProperties.getUploadPath());
    }

    /**
     * PluginFactory for creating instances of Deserializers.
     * @param appProperties Definition of app properties.
     * @return PluginFactory for Deserializers.
     */
    @Bean
    public PluginFactory<Deserializer> getDeserializerPluginFactory(final AppProperties appProperties) {
        final String jarDirectory = appProperties.getUploadPath() + "/deserializers";
        return new PluginFactory<>(jarDirectory, Deserializer.class);
    }

    /**
     * PluginFactory for creating instances of Record Filters.
     * @param appProperties Definition of app properties.
     * @return PluginFactory for Record Filters.
     */
    @Bean
    public PluginFactory<RecordFilter> getRecordFilterPluginFactory(final AppProperties appProperties) {
        final String jarDirectory = appProperties.getUploadPath() + "/filters";
        return new PluginFactory<>(jarDirectory, RecordFilter.class);
    }

    /**
     * For handling secrets, symmetrical encryption.
     * @param appProperties Definition of app properties.
     * @return SecretManager
     */
    @Bean
    public SecretManager getSecretManager(final AppProperties appProperties) {
        return new SecretManager(appProperties.getAppKey());
    }

    /**
     * For creating Kafka Consumers.
     * @param appProperties Definition of app properties.
     * @return Web Kafka Consumer Factory instance.
     */
    @Bean
    public WebKafkaConsumerFactory getWebKafkaConsumerFactory(
            final AppProperties appProperties,
            final KafkaClientConfigUtil configUtil,
            final SecretManager secretManager
    ) {
        final ExecutorService executorService;

        // If we have multi-threaded consumer option enabled
        if (appProperties.isEnableMultiThreadedConsumer()) {
            logger.info("Enabled multi-threaded webconsumer with {} threads.", appProperties.getMaxConcurrentWebConsumers());

            // Create fixed thread pool
            executorService = Executors.newFixedThreadPool(
                    appProperties.getMaxConcurrentWebConsumers(),
                    new ThreadFactoryBuilder()
                            .setNameFormat("kafka-web-consumer-pool-%d")
                            .build()
            );
        } else {
            // Null reference.
            executorService = null;
        }

        return new WebKafkaConsumerFactory(
                getDeserializerPluginFactory(appProperties),
                getRecordFilterPluginFactory(appProperties),
                secretManager,
                getKafkaConsumerFactory(configUtil),
                executorService
        );
    }

    /**
     * For creating Kafka operational consumers.
     * @param configUtil Utility for configuring kafka clients.
     * @param secretManager For managing secrets.
     * @return Web Kafka Operations Client Factory instance.
     */
    @Bean
    public KafkaOperationsFactory getKafkaOperationsFactory(final KafkaClientConfigUtil configUtil, final SecretManager secretManager) {
        return new KafkaOperationsFactory(
                secretManager,
                getKafkaAdminFactory(configUtil)
        );
    }

    /**
     * Customize the jackson object map builder.
     * @return Jackson2ObjectMapperBuilderCustomizer instance.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer registerJacksonProtobufModule() {
        return jacksonObjectMapperBuilder -> {
            // Register custom protocol buffer serializer.
            jacksonObjectMapperBuilder.modulesToInstall(new ProtobufModule());
        };
    }

    /**
     * For creating instances of AdminClient.
     */
    private KafkaAdminFactory getKafkaAdminFactory(final KafkaClientConfigUtil configUtil) {
        return new KafkaAdminFactory(
                configUtil
        );
    }

    /**
     * For creating instances of KafkaConsumers.
     */
    private KafkaConsumerFactory getKafkaConsumerFactory(final KafkaClientConfigUtil configUtil) {
        return new KafkaConsumerFactory(
                configUtil
        );
    }

    /**
     * Utility class for generating common kafka client configs.
     * @param appProperties Definition of app properties.
     * @return KafkaClientConfigUtil
     */
    @Bean
    public KafkaClientConfigUtil getKafkaClientConfigUtil(final AppProperties appProperties) {
        return new KafkaClientConfigUtil(
                appProperties.getUploadPath() + "/keyStores",
                appProperties.getConsumerIdPrefix()
        );
    }

    /**
     * Utility for managing Sasl properties persisted on cluster table.
     * @param secretManager For handling encryption/decryption of secrets.
     * @return SaslUtility instance.
     */
    @Bean
    public SaslUtility getSaslUtility(final SecretManager secretManager) {
        return new SaslUtility(secretManager);
    }

    /**
     * For scrubbing sensitive values from client configs.
     * @param saslUtility instance.
     * @return instance.
     */
    @Bean
    public SensitiveConfigScrubber getSensitiveConfigScrubber(final SaslUtility saslUtility) {
        return new SensitiveConfigScrubber(saslUtility);
    }
}
