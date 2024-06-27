<div align="center">
<h3>Rex</h3>
<strong>
by <a href="https://github.com/Efnilite">Efnilite</a> </strong>
<br><br>
</div>

Welcome to the Rex repo.

### Collaborating

To use this project, you can use **[Jitpack](https://jitpack.io/#efnilite/rex)** to get the source and include it in your preferred build method.

### Structure

Any input first gets sanitized by the Tokenizer, which returns a list of Tokens, which represents the program on the highest level. 
Some tokens can contain other tokens, thus setting up a nested structure. Other tokens contain literals like ints, doubles and strings.
Then, the parser takes this input and evaluates the list of Tokens one by one.
Evaluation means that any Token will be replaced by an instance of the corresponding object, or by a literal.