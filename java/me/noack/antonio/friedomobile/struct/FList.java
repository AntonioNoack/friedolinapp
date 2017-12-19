package me.noack.antonio.friedomobile.struct;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by antonio on 20.11.2017, aber nicht wirklich: in AKISA ist es schon lange Bestandteil
 */

public class FList<I> implements Iterable<I> {
    protected Node next = new Node(null), first = next;// nicht das RAM-freundlichste, aber schnell | das letzte Element
    protected boolean finalized = false;

    @Deprecated
    public void addFirst(I i){
        Node n = new Node(null);
        first.that = i;
        n.next = first;
        first = n;
    }

    public boolean isEmpty(){
        return first.next == null;
    }

    public void clear(){
        if(first.next != null)// not empty
            first = next = new Node(null);
        finalized = false;
    }

    @Override public FList<I> clone(){
        FList<I> cloned = new FList<I>();
        for(I i:this){
            cloned.next = cloned.next.next = new Node(i);
        } return cloned;
    }

    /**
     * kann beliebig oft nach hasNext gefragt werden;
     * next() endet allerdings in einem Error, wenn dem so ist
     * */
    @Override public Iterator<I> iterator() {
        return new Iterator<I>(){

            Node next = first;
            @Override public boolean hasNext() {
                return next.next != null;
            }

            @Override public I next() {
                next = next.next;
                return next.that;
            }

            @Override public void remove() {
                throw new RuntimeException("Shall never be needed. FastLists are designed to be combined, not ereased.");
            }
        };
    }

    /**
     * die andere Liste ist nicht dazu bestimmt weiter verwendet zu werden!
     * */
    public FList<I> addAll(FList<I> toAdd){
        next.next = toAdd.first.next;// damit finden wir von unserem Anfang bis ins Ende von toAdd
        next = toAdd.next;
        toAdd.finalized = true;
        return this;
    }

    static final String error = "added lists to others are not supposed to be filled again before being cleared!";
    public void addAll(Collection<I> toAdd){
        if(finalized){throw new RuntimeException(error);}
        for(I i:toAdd) next = next.next = new Node(i);
    }

    /**
     * Fügt der Liste ein weiteres Element hinzu. Gibt zurück, ob es erfolgreich war.
     * */
    public boolean add(I i){
        if(finalized){throw new RuntimeException(error);}
        next = next.next = new Node(i);
        return true;
    }

    /**
     * nicht sonderlich schnell, und sollte von daher vermieden werden (muss, dank nur einfacher Verkettung, alles durchgehen)
     * */
    public I removeLast(){
        Node n0 = first;

        while(n0.next.next!=null)
            n0 = n0.next;

        Node n1 = n0.next;
        n0.next = null;
        return n1.that;
    }

    public String toString(){
        if(first.next==null)
            return "{}";
        String s = "";
        for(I i:this)s+=i+",";
        return s.substring(0, s.length()-1);
    }

    protected class Node {
        I that;
        Node next;
        public Node(I i){that = i;}
    }

    public I first() {
        return first.next.that;
    }

    @SuppressWarnings("unused")
    public I[] toArray() {
        int i=0;for(I x:this)i++;
        @SuppressWarnings("unchecked")
        I[] ret = (I[]) Array.newInstance(first().getClass(), i);
        i=0;for(I x:this)ret[i++]=x;
        return ret;
    }

    public I last() {
        I last = null;
        for(I i: this){
            last = i;
        } return last;
    }

    /**
     * zählt die Elemente;
     * */
    public int count() {
        int r = 0;
        Node n0 = first;
        while(n0.next != null){
            n0 = n0.next;
            r++;
        } return r;
    }

    public boolean isSingle() {
        return first.next == null || first.next.next==null;
    }

    public FList<I> addAllSingle(FList<I> list) {
        for(I s:list)this.add(s);
        return this;
    }

    public boolean equals(FList<I> that){
        if(that==null) return isEmpty();// halbwegs äquivalent ^^
        Node n0 = first, n1 = that.first;
        if(n0 == null){
            if(n1 == null) return true;
            else return false;
        } else if(n1 == null)
            return false;
        do {
            if(n0.that != n1.that && !n0.that.equals(n1.that))
                return false;

            n0 = n0.next;
            n1 = n1.next;
            if(n0 == null){
                if(n1 == null) return true;
                else return false;
            } else if(n1 == null)
                return false;
        } while(true);
    }

    public void setFirst(I value) {
        first.that = value;
    }
}
