package com.adven.concordion.extensions.exam.mq.redis

import com.adven.concordion.extensions.exam.mq.MqTester
import redis.clients.jedis.Jedis

@Suppress("unused")
open class RedisTester(private val port: Int) : MqTester.NOOP() {
    override fun start() {
        jedis = Jedis("localhost", port)
    }

    override fun send(message: String, headers: Map<String, String>) {
        val kv = message.split("=").toTypedArray()
        jedis[kv[0].trim { it <= ' ' }] = kv[1].trim { it <= ' ' }
    }

    override fun stop() {
        jedis.close()
    }

    companion object {
        private lateinit var jedis: Jedis
    }
}
