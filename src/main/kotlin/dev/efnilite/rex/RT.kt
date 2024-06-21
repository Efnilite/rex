package dev.efnilite.rex

object RT {

    fun add(a: Any?, b: Any?): Number {
        return when (a) {
            is Int -> {
                when (b) {
                    is Int -> a + b
                    is Double -> a + b
                    is Long -> a + b
                    else -> a + b.toString().toDouble()
                }
            }
            is Double -> {
                when (b) {
                    is Int -> a + b
                    is Double -> a + b
                    is Long -> a + b
                    else -> a + b.toString().toDouble()
                }
            }
            is Long -> {
                when (b) {
                    is Int -> a + b
                    is Double -> a + b
                    is Long -> a + b
                    else -> a + b.toString().toLong()
                }
            }
            else -> {
                val parsedA = a.toString().toDouble()

                when (b) {
                    is Int -> parsedA + b
                    is Double -> parsedA + b
                    is Long -> parsedA + b
                    else -> parsedA + b.toString().toDouble()
                }
            }
        }
    }

    fun subtract(a: Any?, b: Any?): Number {
        return when (a) {
            is Int -> {
                when (b) {
                    is Int -> a - b
                    is Double -> a - b
                    is Long -> a - b
                    else -> a - b.toString().toDouble()
                }
            }
            is Double -> {
                when (b) {
                    is Int -> a - b
                    is Double -> a - b
                    is Long -> a - b
                    else -> a - b.toString().toDouble()
                }
            }
            is Long -> {
                when (b) {
                    is Int -> a - b
                    is Double -> a - b
                    is Long -> a - b
                    else -> a - b.toString().toLong()
                }
            }
            else -> {
                val parsedA = a.toString().toDouble()

                when (b) {
                    is Int -> parsedA - b
                    is Double -> parsedA - b
                    is Long -> parsedA - b
                    else -> parsedA - b.toString().toDouble()
                }
            }
        }
    }

    fun multiply(a: Any?, b: Any?): Number {
        return when (a) {
            is Int -> {
                when (b) {
                    is Int -> a * b
                    is Double -> a * b
                    is Long -> a * b
                    else -> a * b.toString().toDouble()
                }
            }
            is Double -> {
                when (b) {
                    is Int -> a * b
                    is Double -> a * b
                    is Long -> a * b
                    else -> a * b.toString().toDouble()
                }
            }
            is Long -> {
                when (b) {
                    is Int -> a * b
                    is Double -> a * b
                    is Long -> a * b
                    else -> a * b.toString().toLong()
                }
            }
            else -> {
                val parsedA = a.toString().toDouble()

                when (b) {
                    is Int -> parsedA * b
                    is Double -> parsedA * b
                    is Long -> parsedA * b
                    else -> parsedA * b.toString().toDouble()
                }
            }
        }
    }

    fun divide(a: Any?, b: Any?): Number {
        return when (a) {
            is Int -> {
                when (b) {
                    is Int -> if (a % b == 0) a / b else a.toDouble() / b
                    is Double -> a / b
                    is Long -> a / b
                    else -> a / b.toString().toDouble()
                }
            }
            is Double -> {
                when (b) {
                    is Int -> a / b
                    is Double -> a / b
                    is Long -> a / b
                    else -> a / b.toString().toDouble()
                }
            }
            is Long -> {
                when (b) {
                    is Int -> a / b
                    is Double -> a / b
                    is Long -> a / b
                    else -> a / b.toString().toLong()
                }
            }
            else -> {
                val parsedA = a.toString().toDouble()

                when (b) {
                    is Int -> parsedA / b
                    is Double -> parsedA / b
                    is Long -> parsedA / b
                    else -> parsedA / b.toString().toDouble()
                }
            }
        }
    }
}