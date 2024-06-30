package dev.efnilite.rex

/**
 * Represents runtime functions.
 *
 * TODO: remove sending scope to every fn lol
 */
@Suppress("unused")
object RT {

    fun iss(x: Any?, cls: Any?): Boolean {
        if (cls !is String) error("Invalid class type")

        return Class.forName(cls).isInstance(x)
    }

    fun iff(cond: Any?, x: Any?, y: Any?, scope: Scope = Scope(null)): Any? {
        return if (cond != null) x else y
    }

    fun count(coll: Any?, scope: Scope = Scope(null)): Int {
        return when (coll) {
            is Arr -> coll.size
            is Mp -> coll.size
            is String -> coll.length
            else -> error("Invalid collection")
        }
    }

    fun get(coll: Any?, key: Any?, scope: Scope = Scope(null)): Any? {
        return when (coll) {
            is Arr -> coll[key as Int]
            is Mp -> coll[key]
            is String -> coll[key as Int]
            else -> error("Invalid collection")
        }
    }

    fun take(n: Any?, coll: Any?, scope: Scope = Scope(null)): Any {
        return when (coll) {
            is Arr -> coll.take(n as Int)
            is String -> coll.take(n as Int)
            else -> error("Invalid collection")
        }
    }

    fun drop(n: Any?, coll: Any?, scope: Scope = Scope(null)): Any {
        return when (coll) {
            is Arr -> coll.drop(n as Int)
            is String -> coll.drop(n as Int)
            else -> error("Invalid collection")
        }
    }

    fun reduce(fn: Any?, initial: Any?, coll: Any?, scope: Scope = Scope(null)): Any? {
        println(coll)
        if (coll !is Arr) error("Invalid collection")

        var acc = initial

        for (element in coll) {
            acc = when (fn) {
                is DefinedFn -> fn.invoke(listOf(acc, element), scope)
                is AnonymousFn -> fn.invoke(listOf(acc, element), scope)
                else -> error("Invalid function")
            }
        }

        return acc
    }

    fun add(a: Any?, b: Any?, scope: Scope = Scope(null)): Any {
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

    fun subtract(a: Any?, b: Any?, scope: Scope = Scope(null)): Any {
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

    fun multiply(a: Any?, b: Any?, scope: Scope = Scope(null)): Any {
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

    fun divide(a: Any?, b: Any?, scope: Scope = Scope(null)): Any {
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