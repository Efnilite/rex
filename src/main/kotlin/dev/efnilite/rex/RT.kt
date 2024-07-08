package dev.efnilite.rex

/**
 * Represents runtime functions.
 *
 * @author <a href='https://efnilite.dev'>Efnilite</a>
 */
@Suppress("unused")
object RT {

    // TODO replace with rx impl
    fun test(name: Any?, fns: Any?): Any {
        if (name !is String) error("name should be a string")
        if (fns !is Arr) error("fns should be an array")

        for (f in fns) {
            when (f) {
                is SFunction -> {
                    if (f.invoke(getCurrentEvaluatingScope()) != true) {
                        throw AssertionError("Test $name failed\n" +
                                "$f returned ${f.invoke(getCurrentEvaluatingScope())}")
                    }
                }
                is Boolean -> {
                    if (!f) {
                        throw AssertionError("Test $name failed\n$f returned $f")
                    }
                }
                else -> error("f should be a function or a boolean")
            }
        }

        return true
    }

    fun throww(message: Any?): Nothing {
        if (message == null) throw IllegalStateException()
        throw IllegalStateException(message.toString())
    }

    fun throws(x: Any?): Boolean {
        if (x !is DeferredFunction) error("x should be an anonymous function")

        try {
            x.invoke(emptyList(), getCurrentEvaluatingScope())
        } catch (e: Exception) {
            return true
        }

        return false
    }

    fun eq(a: Any?, b: Any?): Boolean {
        return a == b
    }

    fun not(x: Any?): Boolean {
        return x == null || x == false
    }

    fun iss(x: Any?, cls: Any?): Boolean {
        if (cls !is String) error("cls should be a string")

        return Class.forName(cls).isInstance(x)
    }

    fun take(n: Any?, coll: Any?): Any {
        return when (coll) {
            is Arr -> coll.take(n as Int)
            is String -> coll.take(n as Int)
            else -> error("coll should be an array or string")
        }
    }

    fun drop(n: Any?, coll: Any?): Any {
        return when (coll) {
            is Arr -> coll.drop(n as Int)
            is String -> coll.drop(n as Int)
            else -> error("coll should be an array or string")
        }
    }

    fun reduce(f: Any?, initial: Any?, coll: Any?): Any? {
        if (coll !is Arr) error("coll should be an array")

        var acc = initial

        for (element in coll) {
            acc = if (f is DeferredFunction) {
                f.invoke(listOf(acc, element), getCurrentEvaluatingScope())
            } else {
                error("Invalid function")
            }
        }

        return acc
    }

    fun reduce(f: Any?, coll: Any?): Any? {
        if (coll !is Arr) error("coll should be an array")

        var acc = coll[0]

        for (i in 1 until coll.size) {
            acc = if (f is DeferredFunction) {
                f.invoke(listOf(acc, coll[i]), getCurrentEvaluatingScope())
            } else {
                error("Invalid function")
            }
        }

        return acc
    }

    fun add(a: Any?, b: Any?): Any {
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

    fun subtract(a: Any?, b: Any?): Any {
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

    fun multiply(a: Any?, b: Any?): Any {
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

    fun divide(a: Any?, b: Any?): Any {
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

    fun mod(a: Any?, b: Any?): Any {
        println(a)
        println(b)
        return when (a) {
            is Int -> {
                when (b) {
                    is Int -> a % b
                    is Double -> a % b
                    is Long -> a % b
                    else -> a % b.toString().toDouble()
                }
            }
            is Double -> {
                when (b) {
                    is Int -> a % b
                    is Double -> a % b
                    is Long -> a % b
                    else -> a % b.toString().toDouble()
                }
            }
            is Long -> {
                when (b) {
                    is Int -> a % b
                    is Double -> a % b
                    is Long -> a % b
                    else -> a % b.toString().toLong()
                }
            }
            else -> {
                val parsedA = a.toString().toDouble()

                when (b) {
                    is Int -> parsedA % b
                    is Double -> parsedA % b
                    is Long -> parsedA % b
                    else -> parsedA % b.toString().toDouble()
                }
            }
        }
    }

    fun pprintln(x: Any?): Unit? {
        println(x)

        return null
    }
}