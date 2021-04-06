package com.adven.concordion.extensions.exam.mq.kafka

import com.adven.concordion.extensions.exam.mq.MqTester
import mu.KLogging
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import java.time.Duration
import java.time.Duration.ofMillis
import java.time.Duration.ofSeconds
import java.util.Properties
import java.util.concurrent.TimeUnit

@Suppress("unused")
open class KafkaTester @JvmOverloads constructor(
    protected val bootstrapServers: String,
    protected val topic: String,
    protected val properties: Properties = DEFAULT_PROPERTIES,
    protected val pollTimeout: Duration = ofMillis(POLL_MILLIS),
    protected val partitionHeader: String = "partition"
) : MqTester {
    protected lateinit var producer: KafkaProducer<Long, String>
    protected lateinit var consumer: KafkaConsumer<Long, String>

    override fun purge() = logger.info("Purging topic {}...", topic).also {
        consumer.poll(ofMillis(POLL_MILLIS))
        logger.info("Topic {} is purged", topic)
    }

    override fun receive(): List<MqTester.Message> = logger.info("Reading from {}", topic).let {
        consumer.poll(pollTimeout).apply { consumer.commitAsync() }.map { MqTester.Message(it.value()) }
    }

    override fun send(message: String, headers: Map<String, String>) = logger.info("Sending to {}...", topic).also {
        producer.send(record(message, partitionFrom(headers))).get().apply {
            logger.info(
                "Sent to topic {} and partition {} with offset {}:\n{}", topic(), partition(), offset(), message
            )
        }
    }

    private fun partitionFrom(headers: Map<String, String>) = headers[partitionHeader]?.toInt()

    private fun record(value: String, partition: Int?): ProducerRecord<Long, String> =
        ProducerRecord(topic, partition, null, value)

    override fun start() {
        properties[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        producer = KafkaProducer<Long, String>(properties)
        consumer = KafkaConsumer<Long, String>(properties).apply { subscribe(listOf(topic)) }
        logger.info("KafkaTester started with properties:\n{}", properties)
    }

    override fun stop() {
        producer.close(ofSeconds(4))
        consumer.close(ofSeconds(4))
    }

    companion object : KLogging() {

        @JvmField
        val DEFAULT_PROPERTIES = Properties().apply {
            put(ProducerConfig.CLIENT_ID_CONFIG, "kafka-tester")
            put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer::class.java.name)
            put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer::class.java.name)
            put(ConsumerConfig.GROUP_ID_CONFIG, "kafka-tester")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        }

        protected const val POLL_MILLIS: Long = 1500
    }
}

@Suppress("unused")
open class KafkaMultiPartitionTester @JvmOverloads constructor(
    private val sutConsumerGroup: String,
    bootstrapServers: String,
    topic: String,
    properties: Properties = DEFAULT_PROPERTIES,
    pollTimeout: Duration = ofMillis(POLL_MILLIS),
    partitionHeader: String = "partition"
) : KafkaTester(bootstrapServers, topic, properties, pollTimeout, partitionHeader) {
    private var adminClient: AdminClient? = null

    override fun start() {
        super.start()
        adminClient = AdminClient.create(properties)
    }

    override fun accumulateOnRetries() = false

    override fun receive(): List<MqTester.Message> = consumer.apply { seekTo(sutOffsets()) }.consume()

    private fun sutOffsets(): Map<TopicPartition, OffsetAndMetadata> =
        adminClient!!.listConsumerGroupOffsets(sutConsumerGroup)
            .partitionsToOffsetAndMetadata()[FETCH_CONSUMER_GROUP_OFFSETS_TIMEOUT, TimeUnit.SECONDS]
            .apply { logger.info("SUT offsets: {}", this) }

    private fun KafkaConsumer<Long, String>.seekTo(offsets: Map<TopicPartition, OffsetAndMetadata>) {
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
        adminClient!!.listOffsets(mapOf(p to OffsetSpec.latest())).all().get()[p]?.offset() ?: 0L

    private fun KafkaConsumer<Long, String>.seekToBeginning() {
        // At this point, there is no heartbeat from consumer and seek() wont work... So call poll() first
        poll(pollTimeout)
        seekToBeginning(assignment())
    }

    private fun KafkaConsumer<Long, String>.seekTo(pointer: Long, p: TopicPartition) {
        // At this point, there is no heartbeat from consumer and seek() wont work... So call poll() first
        poll(pollTimeout)
        seek(p, pointer)
    }

    private fun KafkaConsumer<Long, String>.consume(): List<MqTester.Message> = logger.info("Consuming events...").let {
        poll(pollTimeout).apply { commitAsync() }.map { MqTester.Message(it.value()) }
    }

    companion object : KLogging() {
        private const val POLL_MILLIS: Long = 1500
        private const val FETCH_CONSUMER_GROUP_OFFSETS_TIMEOUT: Long = 10
    }
}
