package io.github.adven27.concordion.extensions.exam.mq.kafka

import io.github.adven27.concordion.extensions.exam.mq.MqTester
import mu.KLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.clients.admin.RecordsToDelete.beforeOffset
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
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
    sutConsumerGroup: String,
    bootstrapServers: String,
    topic: String,
    properties: MutableMap<String, Any?> = (DEFAULT_PRODUCER_CONFIG + DEFAULT_CONSUMER_CONFIG).toMutableMap(),
    pollTimeout: Duration = ofMillis(POLL_MILLIS),
    accumulateOnRetries: Boolean = false
) : KafkaConsumeOnlyTester(sutConsumerGroup, bootstrapServers, topic, properties, pollTimeout, accumulateOnRetries) {
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
        logger.info("Sending to {}...", topic).also {
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
        private const val POLL_MILLIS: Long = 1500
        private const val FETCH_CONSUMER_GROUP_OFFSETS_TIMEOUT: Long = 10
        private const val PARAM_PARTITION = "partition"
        private const val PARAM_KEY = "key"

        @JvmField
        val DEFAULT_PRODUCER_CONFIG: Map<String, String?> = mapOf(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java.name,
        )

        private fun partitionFrom(headers: Map<String, String>) = headers[PARAM_PARTITION]?.toInt()
        private fun keyFrom(headers: Map<String, String>) = headers[PARAM_KEY]
    }
}

@Suppress("unused", "TooManyFunctions")
open class KafkaConsumeOnlyTester @JvmOverloads constructor(
    protected val sutConsumerGroup: String,
    protected val bootstrapServers: String,
    protected val topic: String,
    protected val properties: MutableMap<String, Any?> = DEFAULT_CONSUMER_CONFIG.toMutableMap(),
    protected val pollTimeout: Duration = ofMillis(POLL_MILLIS),
    protected val accumulateOnRetries: Boolean = false
) : MqTester {
    protected lateinit var consumer: KafkaConsumer<String, String>
    protected lateinit var adminClient: AdminClient

    override fun accumulateOnRetries() = accumulateOnRetries

    override fun start() {
        properties[ConsumerConfig.GROUP_ID_CONFIG] = "kafka-tester-$topic"
        consumer = KafkaConsumer<String, String>(properties).apply { subscribe(listOf(topic)) }
        adminClient = AdminClient.create(properties)
        logger.info("KafkaTester started with properties:\n{}", properties)
    }

    override fun stop() {
        consumer.close(ofSeconds(4))
    }

    override fun purge() = logger.info("Purging topic {}...", topic).also {
        adminClient.deleteRecords(sutOffsets().map { it.key to beforeOffset(it.value.offset()) }.toMap())
        logger.info("Topic {} is purged", topic)
    }

    override fun receive(): List<MqTester.Message> = consumer.apply { seekTo(sutOffsets()) }.consume()

    override fun send(message: MqTester.Message, params: Map<String, String>) {
        throw UnsupportedOperationException("$javaClass doesn't support sending messages")
    }

    private fun sutOffsets(): Map<TopicPartition, OffsetAndMetadata> =
        adminClient.listConsumerGroupOffsets(sutConsumerGroup)
            .partitionsToOffsetAndMetadata()[FETCH_CONSUMER_GROUP_OFFSETS_TIMEOUT, TimeUnit.SECONDS]
            .apply { logger.info("SUT offsets: {}", this) }

    private fun KafkaConsumer<String, String>.seekTo(offsets: Map<TopicPartition, OffsetAndMetadata>) {
        if (offsets.isEmpty()) {
            seekToBeginning()
        } else {
            offsets.entries.map { it.key to it.value.offset() }.forEach { (partition, committed) ->
                val end = endOf(partition)
                logger.info("Committed offset: {} {}", "$committed/$end", partition)
                if (committed != end) seekTo(committed, partition)
            }
        }
    }

    private fun endOf(p: TopicPartition): Long =
        adminClient.listOffsets(mapOf(p to OffsetSpec.latest())).all().get()[p]?.offset() ?: 0L

    private fun KafkaConsumer<String, String>.seekToBeginning() {
        // At this point, there is no heartbeat from consumer and seek() wont work... So call poll() first
        poll(pollTimeout)
        seekToBeginning(assignment())
    }

    private fun KafkaConsumer<String, String>.seekTo(pointer: Long, p: TopicPartition) {
        // At this point, there is no heartbeat from consumer and seek() wont work... So call poll() first
        poll(pollTimeout)
        seek(p, pointer)
    }

    private fun KafkaConsumer<String, String>.consume(): List<MqTester.Message> =
        logger.info("Consuming events...").let {
            poll(pollTimeout).apply { commitAsync() }.map { MqTester.Message(it.value()) }
        }

    companion object : KLogging() {
        private const val POLL_MILLIS: Long = 1500
        private const val FETCH_CONSUMER_GROUP_OFFSETS_TIMEOUT: Long = 10

        @JvmField
        val DEFAULT_CONSUMER_CONFIG: Map<String, String?> = mapOf(
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java.name,
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        )
    }
}
