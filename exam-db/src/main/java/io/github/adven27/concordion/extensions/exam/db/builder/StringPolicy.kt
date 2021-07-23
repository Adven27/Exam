package io.github.adven27.concordion.extensions.exam.db.builder

interface IStringPolicy {
    fun areEqual(first: String, second: String): Boolean
    fun toKey(value: String): String
}

class CaseInsensitiveStringPolicy : IStringPolicy {
    override fun areEqual(first: String, second: String): Boolean = first.equals(second, ignoreCase = true)
    override fun toKey(value: String): String = value.uppercase()
}

class CaseSensitiveStringPolicy : IStringPolicy {
    override fun areEqual(first: String, second: String): Boolean = first == second
    override fun toKey(value: String): String = value
}
