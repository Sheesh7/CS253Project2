package CommonUtils;

import java.util.Arrays;

/**
 * Implements our MinHeapInterface and adds a constructor
 * <p>
 * <b>251 students: You are explicitly forbidden from using java.util.Queue (including any subclass
 *   like PriorityQueue) and any other java.util.* library EXCEPT java.util.Arrays and java.util.Vector.
 *   Write your own implementation of a MinHeap.</b>
 *
 * @param <E> the type of object this heap will be holding
 */
public class MinHeap<E extends Comparable<E>> implements MinHeapInterface<E> {
    private E[] heap;
    private int size;
    private static final int DEFAULT_CAPACITY = 10;

    /**
     * A recursive method to heapify (sort the root to where it should go) a
     *   subtree with the root at given index
     * Assumes the subtrees are already heapified.
     * (The purpose of this method is to balance tree starting at the root)
     * @param i root of the subtree to heapify
     */
    private void heapify(int i) {
        int smallest = i;
        int left = 2 * i + 1;
        int right = 2 * i + 2;
        if (left < size){
            if (heap[left].compareTo(heap[smallest]) < 0){
                smallest = left;
            }
        }
        if (right < size){
            if (heap[right].compareTo(heap[smallest]) < 0){
                smallest = right;
            }
        }
        if (smallest != i) {
        swap(i, smallest);
        heapify(smallest);
        }
    }

    /**
     * Constructs an empty min heap
     */
    public MinHeap(){
        heap = (E[]) new Comparable[DEFAULT_CAPACITY];
        size = 0;
    }

    /**
     * Adds the item to the min heap
     *
     * @param item item to add
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public void add(E item) {
        if (item == null) throw new NullPointerException();
        if (size == heap.length) {
            resize();
        }
        heap[size] = item;
        bubbleUp(size);
        size++;
    }

    /**
     * Empties the heap.
     */
    @Override
    public void clear() {
        Arrays.fill(heap, null);
        size = 0;
    }

    /**
     * Returns the minimum element without removing it, or returns <code>null</code> if heap is empty
     *
     * @return the minimum element in the heap, or <code>null</code> if heap is empty
     */
    @Override
    public E peekMin() {
        if (size == 0){
            return null;
        }
        return heap[0];       
    }

    /**
     * Remove and return the minimum element in the heap, or returns <code>null</code> if heap is empty
     *
     * @return the minimum element in the heap, or <code>null</code> if heap is empty
     */
    @Override
    public E removeMin() {
        if (size == 0){
            return null;
        }
        E min = heap[0];
        heap[0] = heap[size-1];
        heap[size - 1] = null;
        size--;
        if (size > 0){
            heapify(0);
        }
        return min;
//        return null;
    }

    /**
     * Returns the number of elements in the heap
     *
     * @return integer representing the number of elements in the heap
     */
    @Override
    public int size() {
        return size;
    }
    //Extra helper methods
    private void bubbleUp(int i){
        int parent = (i-1) / 2;
        while (i > 0) {
            if (heap[i].compareTo(heap[parent]) < 0) {
                swap(i, parent);
                i = parent;
                parent = (i - 1) / 2;
            } else {
                break;
            }
        }
    }
    private void swap(int i, int j){
        E temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
    private void resize() {
        heap = Arrays.copyOf(heap, heap.length * 2);
    }
}