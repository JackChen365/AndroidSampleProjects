## Readme

The first step of the chunk text layout is load the text lazily.
Just like how we read the text line from the buffered stream.

```
val bufferdStream=BufferedReader(...)
bufferedStream.readLine();
```

When you do something like this. You get the text line whenever you want.
by contrast, The text layout will calculate the text layout information all at once.
Therefore, The first thing I should consider was: Keep a buffer inside the layout and calculate the text layout information lazily.

