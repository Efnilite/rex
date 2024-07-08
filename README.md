<div align="center">
<h3>Rex</h3>
<strong>
by <a href="https://github.com/Efnilite">Efnilite</a> </strong>
<br><br>
</div>

Welcome to the Rex repo.

### Collaborating

To use this project, you can use **[Jitpack](https://jitpack.io/#efnilite/rex)** to get the source and include it in your preferred build method.

### Features

Use `var` or `val` to specify (mutable) variables and (immutable) values. 
Use `set` to change the value of a variable.

```clojure
(val x nil)
(var map+ (fn [coll] (map inc coll)))

(if (nil x?)
    (set map+ (fn [coll] (map dec coll))))
    
(map+ [1 2 3])
```

`use` clears all prior variable or value definitions, except ones you specify.

```clojure
(val x 1)
(use [] (+ x 2)) # unknown variable error
(use [x] (+ x 2)) # returns 3
```