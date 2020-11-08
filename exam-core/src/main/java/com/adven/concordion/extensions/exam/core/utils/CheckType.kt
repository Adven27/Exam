package com.adven.concordion.extensions.exam.core.utils

enum class CheckType {
    NUMBER, STRING, BOOLEAN;

    fun suit(node: String): Boolean {
        return when (this) {
            NUMBER -> isNum(node)
            BOOLEAN -> {
                val value = node.toLowerCase()
                "true" == value || "false" == value
            }
            STRING -> !isNum(node)
        }
    }

    private fun isNum(node: String): Boolean {
        return node.toIntOrNull() != null || node.toDoubleOrNull() != null
    }
}
