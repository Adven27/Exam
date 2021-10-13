package io.github.adven27.concordion.extensions.exam.core.utils

enum class CheckType {
    NUMBER, STRING, BOOLEAN;

    fun suit(node: String): Boolean = when (this) {
        NUMBER -> isNum(node)
        BOOLEAN -> node.lowercase().let { "true" == it || "false" == it }
        STRING -> !isNum(node)
    }

    private fun isNum(node: String): Boolean = node.toIntOrNull() != null || node.toDoubleOrNull() != null
}
