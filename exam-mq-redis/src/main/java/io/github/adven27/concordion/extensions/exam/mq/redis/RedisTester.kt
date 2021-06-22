package io.github.adven27.concordion.extensions.exam.mq.redis

import io.github.adven27.concordion.extensions.exam.mq.MqTester
import redis.clients.jedis.Jedis

@Suppress("unused")
open class RedisTester(private val host: String, private val port: Int) : MqTester.NOOP() {
    override fun start() {
        jedis = Jedis(host, port)
    }

    override fun send(message: MqTester.Message, params: Map<String, String>) {
        val kv = message.body.split("=").toTypedArray()
        jedis[kv[0].trim { it <= ' ' }] = kv[1].trim { it <= ' ' }
    }

    override fun stop() {
        jedis.close()
    }

    companion object {
        private lateinit var jedis: Jedis
    }
}
