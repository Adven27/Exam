package io.github.adven27.concordion.extensions.exam.mq.kafka

import io.github.adven27.concordion.extensions.exam.mq.MqTester
import mu.KLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.clients.admin.RecordsToDelete
import org.apache.kafka.clients.admin.TopicDescription
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.header.internals.RecordHeader
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds
import java.util.concurrent.TimeUnit

@Suppress("unused")
open class KafkaConsumeAndSendTester @JvmOverloads constructor(
    bootstrapServers: String,
    topic: String,
    sutConsumerGroup: String? = null,
    properties: MutableMap<String, Any?> = (DEFAULT_PRODUCER_CONFIG + DEFAULT_CONSUMER_CONFIG).toMutableMap(),
    pollTimeout: Duration = ofMillis(POLL_MILLIS),
    accumulateOnRetries: Boolean = false
) : KafkaConsumeOnlyTester(bootstrapServers, topic, sutConsumerGroup, properties, pollTimeout, accumulateOnRetries) {
    protected lateinit var producer: KafkaProducer<String, String>

    override fun start() {
        properties[ProducerConfig.CLIENT_ID_CONFIG] = "kafka-tester-$topic"
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        producer = KafkaProducer<String, String>(properties)
        super.start()
    }

    override fun stop() {
        producer.close(ofSeconds(4))
        super.stop()
    }

    override fun send(message: MqTester.Message, params: Map<String, String>) =
        logger.debug("Sending to {}...", topic).also {
            producer.send(record(message, partitionFrom(params), keyFrom(params))).get().apply {
                logger.info(
                    "Sent to topic {} and partition {} with offset {}:\n{}", topic(), partition(), offset(), message
                )
            }
        }

    private fun record(message: MqTester.Message, partition: Int?, key: String?) = ProducerRecord(
        topic, partition, key, message.body, message.headers.map { RecordHeader(it.key, it.value.toByteArray()) }
    )

    companion object : KLogging() {
        private const val PARAM_PARTITION = "partition"
        private const val PARAM_KEY = "key"

        @JvmField
        val DEFAULT_PRODUCER_CONFIG: Map<String, String?> = mapOf(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.ACKS_CONFIG to "all",
            ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
        )

        private fun partitionFrom(headers: Map<String, String>) = headers[PARAM_PARTITION]?.toInt()
        private fun keyFrom(headers: Map<String, String>) = headers[PARAM_KEY]
    }
}

@Suppress("unused", "TooManyFunctions")
open class KafkaConsumeOnlyTester @JvmOverloads constructor(
    protected val bootstrapServers: String,
    protected val topic: String,
    protected val sutConsumerGroup: String?,
    protected val properties: MutableMap<String, Any?> = DEFAULT_CONSUMER_CONFIG.toMutableMap(),
    protected val pollTimeout: Duration = ofMillis(POLL_MILLIS),
    protected val accumulateOnRetries: Boolean = false
) : MqTester {
    protected lateinit var consumer: KafkaConsumer<String, String>
    protected lateinit var adminClient: AdminClient

    override fun accumulateOnRetries() = accumulateOnRetries

    override fun start() {
        properties[ConsumerConfig.GROUP_ID_CONFIG] = "kafka-tester-$topic"
        consumer = KafkaConsumer<String, String>(properties).apply {
            assign(partitionsFor(topic).map { TopicPartition(it.topic(), it.partition()) })
        }
        adminClient = AdminClient.create(properties)
        logger.info("KafkaTester started with properties:\n{}", properties)
    }

    override fun stop() {
        consumer.close(ofSeconds(4))
    }

    override fun purge() = logger.debug("Purging topic {}...", topic).also {
        adminClient.deleteRecords(
            listLatestOffsets()
                .map { it.key to RecordsToDelete.beforeOffset(it.value.offset()) }
                .associate { it.apply { logger.debug("Purging partition {}", this) } }
        )
        logger.info("Topic {} is purged", topic)
    }

    private fun listLatestOffsets() = adminClient.listOffsets(latestOffsetsSpec())
        .all()[KAFKA_FETCHING_TIMEOUT, TimeUnit.SECONDS]

    private fun latestOffsetsSpec(): Map<TopicPartition, OffsetSpec> = getPartitionsFor(topic)
        .associate { TopicPartition(topic, it) to OffsetSpec.latest() }

    private fun getPartitionsFor(topic: String) = adminClient.describeTopics(listOf(topic))
        .values().values.flatMap { it.toPartitions() }

    private fun KafkaFuture<TopicDescription>.toPartitions() =
        this[KAFKA_FETCHING_TIMEOUT, TimeUnit.SECONDS].partitions().map { it.partition() }

    override fun receive(): List<MqTester.Message> = consumer.apply { seekTo(sutOffsets()) }.consume()

    override fun send(message: MqTester.Message, params: Map<String, String>) {
        throw UnsupportedOperationException("$javaClass doesn't support sending messages")
    }

    private fun sutOffsets(): Map<TopicPartition, OffsetAndMetadata> =
        if (sutConsumerGroup == null) emptyMap()
        else adminClient.listConsumerGroupOffsets(sutConsumerGroup)
            .partitionsToOffsetAndMetadata()[KAFKA_FETCHING_TIMEOUT, TimeUnit.SECONDS]
            .filterKeys { it.topic() == topic }
            .apply { logger.debug("SUT [consumer group: {}] offsets: {}", sutConsumerGroup, this) }

    private fun KafkaConsumer<String, String>.seekTo(offsets: Map<TopicPartition, OffsetAndMetadata>) {
        if (offsets.isEmpty()) {
            logger.debug("Offsets are empty - seek to beginning...").also { seekToBeginning(assignment()) }
        } else {
            offsets.entries.forEach { (partition, committed) ->
                logger.debug("Seek to committed offset: {} in {}", committed, partition)
                seek(partition, committed)
            }
        }
    }

    private fun endOf(p: TopicPartition): Long =
        adminClient.listOffsets(mapOf(p to OffsetSpec.latest())).all().get()[p]?.offset() ?: 0L

    private fun KafkaConsumer<String, String>.consume(): List<MqTester.Message> =
        logger.debug("Consuming events... {}ms", pollTimeout.toMillis()).let {
            poll(pollTimeout).map {
                MqTester.Message(it.value()).apply {
                    logger.info("Event consumed:\n{}", this)
                }
            }
        }

    companion object : KLogging() {
        const val POLL_MILLIS: Long = 50
        private const val KAFKA_FETCHING_TIMEOUT: Long = 10

        @JvmField
        val DEFAULT_CONSUMER_CONFIG: Map<String, String?> = mapOf(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        )
    }
}
