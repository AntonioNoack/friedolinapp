package me.noack.antonio.friedomobile.struct;

/**
 * Created by antonio on 25.11.2017
 */

public class Twig<I> {

    public FList<Twig<I>> children;
    public I value;
    public Twig(I value){
        this.value = value;
    }

    private Twig<I> parent;

    public Twig<I> end(){
        return parent;
    }

    public Twig<I> sub(I value){
        if(children == null){children = new FList<>();}
        Twig<I> child = new Twig<I>(value);
        child.parent = this;
        children.add(child);
        return child;
    }
}
