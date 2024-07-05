package dev.efnilite.rex

/**
 * Represents runtime functions.
 *
 * TODO: remove sending scope to every fn lol
 * @author <a href='https://efnilite.dev'>Efnilite</a>
 */
@Suppress("unused")
object RT {

    // TODO replace with rx impl
    fun test(name: Any?, fns: Any?, scope: Scope = Scope(null)): Any {
        if (name !is String) error("name should be a string")
        if (fns !is Arr) error("fns should be an array")

        for (fn in fns) {
            when (fn) {
                is Fn -> {
                    if (fn.invoke(scope) != true) {
                        throw AssertionError("Test $name failed\n$fn returned ${fn.invoke(scope)}")
                    }
                }
                is Boolean -> {
                    if (!fn) {
                        throw AssertionError("Test $name failed\n$fn returned $fn")
                    }
                }
                else -> error("fn should be a function or a boolean")
            }
        }

        return true
    }

    fun throww(message: Any?, scope : Scope = Scope(null)): Nothing {
        if (message == null) throw IllegalStateException()
        throw IllegalStateException(message.toString())
    }

    fun throws(x: Any?, scope: Scope = Scope(null)): Boolean {
        if (x !is AFn) error("x should be an anonymous function")

        try {
            x.invoke(emptyList(), scope)
        } catch (e: Exception) {
            return true
        }

        return false
    }

    fun eq(a: Any?, b: Any?, scope: Scope = Scope(null)): Boolean {
        return a == b
    }

    fun not(x: Any?, scope: Scope = Scope(null)): Boolean {
        return x == null || x == false
    }

    fun iss(x: Any?, cls: Any?, scope: Scope = Scope(null)): Boolean {
        if (cls !is String) error("cls should be a string")

        return Class.forName(cls).isInstance(x)
    }

    fun iff(cond: Any?, x: Any?, y: Any?, scope: Scope = Scope(null)): Any? {
        return if (cond != false && cond != null) x else y
    }

    fun count(coll: Any?, scope: Scope = Scope(null)): Int {
        return when (coll) {
            is Arr -> coll.size
            is Mp -> coll.size
            is String -> coll.length
            else -> error("coll should be an array, map or string")
        }
    }

    fun get(coll: Any?, key: Any?, scope: Scope = Scope(null)): Any? {
        return when (coll) {
            is Arr -> coll[key as Int]
            is Mp -> coll[key]
            is String -> coll[key as Int]
            else -> error("coll should be an array, map or string")
        }
    }

    fun take(n: Any?, coll: Any?, scope: Scope = Scope(null)): Any {
        return when (coll) {
            is Arr -> coll.take(n as Int)
            is String -> coll.take(n as Int)
            else -> error("coll should be an array or string")
        }
    }

    fun drop(n: Any?, coll: Any?, scope: Scope = Scope(null)): Any {
        return when (coll) {
            is Arr -> coll.drop(n as Int)
            is String -> coll.drop(n as Int)
            else -> error("coll should be an array or string")
        }
    }

    fun reduce(fn: Any?, initial: Any?, coll: Any?, scope: Scope = Scope(null)): Any? {
        if (coll !is Arr) error("coll should be an array")

        var acc = initial

        for (element in coll) {
            acc = if (fn is Invocable) fn.invoke(listOf(acc, element), scope) else error("Invalid function")
        }

        return acc
    }

    fun reduce(fn: Any?, coll: Any?, scope: Scope = Scope(null)): Any? {
        if (coll !is Arr) error("coll should be an array")

        var acc = coll[0]

        for (i in 1 until coll.size) {
            acc = if (fn is Invocable) fn.invoke(listOf(acc, coll[i]), scope) else error("Invalid function")
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