## DependencyGraph

What I want to do is visualize all the data structures. But for a data structure like HashMap. How to visualize it?

For example: for an array.
```
[1,2,3,4,5]
```

It very easy to build the dependency graph like this

```
[parent <-1->  <-2-> <-3-> <-4-> <-5-> parent]
```

But how to deal with map/binary tree?

The first crucial point was how to return all the elements in the collection.
That could easily come up with Iterable. Therefore It is not a problem for us.

So the only problem was how to know the relation between each element.

Since we have to process every single element. That is mean We want to know each element.

It could simply write a function like
```
Relative[] getRelation(E e)
```

The result was a combination of an array of relative elements.

The class prototype may look like this

```
class Relative{
    E item;
    int flag; // left_to_left|top_to_top|right_to_left...
}
```

But how to get the relatives from the specific class like: HashMap???
First, hashMap could change frequently. You have no idea when it will expand its capacity.
All the details inside the Class. So what should we do to know the relationship between each element?


Sounds like we can not just use the data structure in the Java develop kit.
We should have our own data structure to describe the relation between each nodes.

The declaration probably looks like this:

```
interface GraphDemonstrable{

    Node<E> getRootNode();

    List<Node<E>> getRelatives(E item);

    List<Node<E>> relativesFromLeft(E item);

    List<Node<E>> relativesFromTop(E item);

    List<Node<E>> relativesFromRight(E item);

    List<Node<E>> relativesFromBottom(E item);

    int getGraphBreadth();

    int getGraphDepth();
}
```

In that case, We could be able to organize the graph by ourselves.
After I have finished the HashMap. I will built the graph like this:

```
[1,2,3,4]
 a d f g
 b e   h
 c

```
It begins from the root element: 1. From the number:1 you invoke the method:getRelatives()

you will probably get a list contains the number:2 and the letter:a, keep calling the method until you build the dependency graph.








