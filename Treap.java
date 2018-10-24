package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

//Every node has a key and a priority and a priority.
//The keys must satisfy the BST property, the priorities must satisfy the Max Heap property.

public class Treap<K, V> extends AbstractMap<K, V> implements SortedMap<K, V> {

	private TreapNode<K, V> root;
	// private TreeSet<Map.Entry<K,V>> entryHolder = new
	// TreeSet<Map.Entry<K,V>>(new
	// TreapNodeComparator2());
	private int size = 0;
	private int modCount = 0;
	private Comparator<K> comp;
	private Random rng = new Random();

	public Treap(Comparator<K> e) {
		super();
		comp = e;
	}

	public Treap() {
		super();
		comp = null;
		root = null;
	}

	public Treap(Map m) {
		super();
		// entryHolder = (TreeSet<java.util.Map.Entry<K, V>>) m.entrySet();

	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public void clear() {
		size = 0;
		root = null;
		this.entrySet().clear();
		// entryHolder.clear();
	}

	public Element printTreap(Document results) {
		return root.printNode(results);
	}

	@Override
	public V get(Object key) {

		if (key == null)
			throw new NullPointerException();

		return super.get(key);
	}

	@Override
	public boolean containsKey(Object o) {

		if (o == null) {
			throw new NullPointerException();
		}

		if (this.size == 0) {
			return false;
		}

		if (root == null) {
			return false;
		}

		K key = (K) o;

		if (comp != null) {
			TreapNode<K, V> t = root;
			return containsKeyHelper(key, t, comp);
		} else {

			if (key == null)
				return false;

			TreapNode<K, V> t = root;
			return containsKeyHelper(key, t);
		}
	}

	private boolean containsKeyHelper(K key, TreapNode<K, V> t, Comparator<K> comparator) {
		if (t.getKey().equals(key)) {
			return true;
		}
		int comparedResult = comparator.compare(key, t.getKey());

		if (t.getRightChild() != null && comparedResult > 0) {
			return containsKeyHelper(key, t.getRightChild(), comparator);
		} else if (t.getLeftChild() != null && comparedResult < 0) {
			return containsKeyHelper(key, t.getLeftChild(), comparator);
		}
		return false;

	}

	private boolean containsKeyHelper(K key, TreapNode<K, V> t) {

		if (t.getKey().equals(key)) {
			return true;
		}

		Comparable<? super K> k = (Comparable<? super K>) key;

		int comparedResult = k.compareTo((t.getKey()));

		if (t.getRightChild() != null && comparedResult > 0) {
			return containsKeyHelper(key, t.getRightChild());
		} else if (t.getLeftChild() != null && comparedResult < 0) {
			return containsKeyHelper(key, t.getLeftChild());
		}
		return false;

	}

	private int compare(Object key1, Object key2) {

		K k1 = (K) key1;
		K k2 = (K) key2;

		if (comp == null) {

			Comparable<? super K> key = (Comparable<? super K>) k1;

			return key.compareTo(k2);
		} else {
			return comp.compare(k1, k2);
		}
	}

	@Override
	public String toString() {

		return super.toString();

		// Set<java.util.Map.Entry<K, V>> holder = this.entrySet();
		// return ((Treap<K, V>.EntrySet) holder).toTreapString();
		// return "{" + root.toContinuedString() + "}";
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (o == this)
			return true;

		if (!(o instanceof Map))
			return false;

		Map other = (Map) o;

		if (other.size() == this.size()) {

			return this.entrySet().equals(other.entrySet());

		} else {
			return false;
		}
	}

	/*
	 * @Override public int hashCode(){ int result = 0;
	 * 
	 * for(Entry<K, V> c : entryHolder){ result = result + (c.hashCode()); }
	 * 
	 * return result; }
	 * 
	 * @Override public boolean equals(Object o){ if(!(o instanceof Map)){
	 * 
	 * if(o instanceof Set){ return this.toString().equals(o.toString()); }
	 * 
	 * return false; } else { Map<K, V> other = (Map<K, V>) o;
	 * 
	 * TreeSet<Entry> secondHolder = new TreeSet<Entry>(new
	 * TreapNodeComparator2());
	 * 
	 * secondHolder.addAll(entryHolder);
	 * 
	 * Iterator<java.util.Map.Entry<K, V>> it = other.entrySet().iterator();
	 * 
	 * for(Entry c : secondHolder){
	 * 
	 * if(!it.hasNext()){ return false; }
	 * 
	 * if(!c.equals(it.next())){ return false; } } }
	 * 
	 * return true; }
	 */

	@Override
	public V put(K key, V value) {

		TreapNode<K, V> newNode;

		if (comp == null) {
			newNode = new TreapNode<K, V>(key, value, rng.nextInt(), null, null, null);
		} else {
			newNode = new TreapNode<K, V>(key, value, rng.nextInt(), null, null, null, comp);
		}

		if (root == null) {
			root = newNode;
			size = 1;
			modCount++;

			return value;
		} else {// Need to not increment size if already present, just remapping
			if (this.containsKey(key))
				size--;
			root.put(newNode);

		}

		while (newNode.getParent() != null && newNode.getParent().getPriority() < newNode.getPriority()) {// Heap
																											// property
																											// violated.

			if (newNode.getParent().getLeftChild() != null && newNode.getParent().getLeftChild().equals(newNode)) {
				// Rotate right
				rotateRight(newNode.getParent());
			} else if (newNode.getParent().getRightChild() != null
					&& newNode.getParent().getRightChild().equals(newNode)) {
				// Rotate left
				rotateLeft(newNode.getParent());
			}

		}

		size++;
		modCount++;
		return value;
	}

	/*
	 * @Override public V put(K key, V value){ //Make a new TreapNode //Insert
	 * it into the map //Call separate rotator method on new node to see if it
	 * needs to be moved.
	 * 
	 * if(key == null || value == null){ throw new NullPointerException(); }
	 * 
	 * if(size() == 0){//If treap is empty. root = new TreapNode<K,V>(key,
	 * value, rng.nextInt(), null, null, null); size = 1; modCount++;
	 * entryHolder.add(root); return value; }
	 * 
	 * TreapNode<K, V> newNode;
	 * 
	 * if(comp == null){ newNode = new TreapNode<K,V>(key, value, rng.nextInt(),
	 * null, null, null); } else { newNode = new TreapNode<K,V>(key, value,
	 * rng.nextInt(), null, null, null, comp); }
	 * 
	 * 
	 * root.put(newNode);
	 * 
	 * if(newNode.getParent() != null && newNode.getPriority() >
	 * newNode.getParent().getPriority()){
	 * 
	 * if(newNode.getParent().getRightChild() == newNode){ rotateLeft(newNode);
	 * 
	 * } else if(newNode.getParent().getLeftChild() == newNode){
	 * rotateRight(newNode);
	 * 
	 * }
	 * 
	 * if(newNode.getParent() == null){ root = newNode; newNode.setParent(null);
	 * }
	 * 
	 * }
	 * 
	 * entryHolder.add(newNode); size++; modCount++; return value;
	 * 
	 * }
	 */

	private void rotateLeft(TreapNode<K, V> parent) {

		TreapNode<K, V> child = parent.getRightChild();

		TreapNode<K, V> grandparent = parent.getParent();
		boolean isLeft = false, isRight = false;

		if (grandparent != null) {

			if (grandparent.getLeftChild() != null && grandparent.getLeftChild().equals(parent)) {
				isLeft = true;
			} else if (grandparent.getRightChild() != null && grandparent.getRightChild().equals(parent)) {
				isRight = true;
			}

		}

		if (child.getLeftChild() != null) {
			parent.setRight(child.getLeftChild());
			child.getLeftChild().setParent(parent);
		} else {
			parent.setRight(null);
		}

		child.setLeft(parent);
		parent.setParent(child);

		if (grandparent != null) {
			if (isLeft) {
				grandparent.setLeft(child);
			} else if (isRight) {
				grandparent.setRight(child);
			}
			child.setParent(grandparent);

		} else {
			child.setParent(null);// Child is now root.
			root = child;
		}

		/*
		 * TreapNode<K,V> child = parent.getRightChild();
		 * 
		 * TreapNode<K,V> grandparent = parent.getParent();
		 * 
		 * child.setParent(grandparent);
		 * 
		 * if(child.getParent() != null){
		 * 
		 * if(child.getParent().getLeftChild() == parent){
		 * child.getParent().setLeft(child); } else {
		 * child.getParent().setRight(child); } }
		 * 
		 * parent.setRight(child.getLeftChild());
		 * 
		 * if(parent.getRightChild() != null){
		 * parent.getRightChild().setParent(parent); }
		 * 
		 * parent.setParent(child); child.setLeft(parent);
		 * 
		 * if(parent == root) { root = child; child.setParent(null); }
		 * 
		 */
	}

	private void rotateRight(TreapNode<K, V> parent) {

		TreapNode<K, V> child = parent.getLeftChild();

		TreapNode<K, V> grandparent = parent.getParent();

		boolean isLeft = false, isRight = false;

		if (grandparent != null) {

			if (grandparent.getLeftChild() != null && grandparent.getLeftChild().equals(parent)) {
				isLeft = true;
			} else if (grandparent.getRightChild() != null && grandparent.getRightChild().equals(parent)) {
				isRight = true;
			}

		}

		if (child.getRightChild() != null) {
			parent.setLeft(child.getRightChild());
			child.getRightChild().setParent(parent);
		} else {
			parent.setLeft(null);
		}

		child.setRight(parent);
		parent.setParent(child);

		if (grandparent != null) {
			if (isLeft) {
				grandparent.setLeft(child);
			} else if (isRight) {
				grandparent.setRight(child);
			}
			child.setParent(grandparent);

		} else {
			child.setParent(null);// Child is now root.
			root = child;
		}

		/*
		 * TreapNode<K,V> child = parent.getLeftChild();
		 * 
		 * TreapNode<K,V> grandparent = parent.getParent();
		 * 
		 * child.setParent(grandparent);
		 * 
		 * 
		 * if(child.getParent() != null) {
		 * 
		 * if(child.getParent().getLeftChild() == parent){
		 * child.getParent().setLeft(child); } else {
		 * child.getParent().setRight(child); } }
		 * 
		 * parent.setLeft(child.getRightChild());
		 * 
		 * if(parent.getLeftChild() != null){
		 * parent.getLeftChild().setParent(parent); }
		 * 
		 * parent.setParent(child); child.setRight(parent); if(parent == root){
		 * root = child; child.setParent(null); }
		 */
	}

	@Override
	public V remove(Object o) {
		throw new UnsupportedOperationException();
		/*
		 * K key = (K) o; int comparedResult;
		 * 
		 * TreapNode<K, V> t = root; TreapNode<K, V> toDelete = null;
		 * 
		 * if (comp != null) { do { comparedResult = comp.compare(key,
		 * t.getKey());
		 * 
		 * if (comparedResult > 0) t = t.getRightChild(); if (comparedResult <
		 * 0) t = t.getLeftChild();
		 * 
		 * if (comparedResult == 0) { toDelete = t; }
		 * 
		 * }
		 * 
		 * while (t != null); }
		 * 
		 * else { Comparable<? super K> k = (Comparable<? super K>) key;
		 * 
		 * do { comparedResult = k.compareTo(t.getKey());
		 * 
		 * if (comparedResult > 0) t = t.getRightChild(); if (comparedResult <
		 * 0) t = t.getLeftChild();
		 * 
		 * if (comparedResult == 0) { toDelete = t; } }
		 * 
		 * while (t != null); }
		 * 
		 * if (toDelete == null) { return null; }
		 * 
		 * boolean leftGreaterThanRight;
		 * 
		 * while (toDelete.getLeftChild() != null && toDelete.getRightChild() !=
		 * null) {// Keep rotating until its a leaf
		 * 
		 * if (toDelete.getLeftChild().getPriority() >=
		 * toDelete.getRightChild().getPriority()) { leftGreaterThanRight =
		 * true; } else { leftGreaterThanRight = false; }
		 * 
		 * if (leftGreaterThanRight) { rotateRight(toDelete);
		 * 
		 * } else if (!leftGreaterThanRight) { rotateLeft(toDelete);
		 * 
		 * } }
		 * 
		 * V value = toDelete.getValue();
		 * 
		 * if (toDelete.getParent().getLeftChild() == toDelete) {
		 * toDelete.getParent().setLeft(null); size--; //
		 * entryHolder.remove(toDelete); modCount++; return value; } else if
		 * (toDelete.getParent().getRightChild() == toDelete) {
		 * toDelete.getParent().setRight(null); size--; modCount++; return
		 * value; }
		 * 
		 * return null;
		 */
	}

	@Override
	public Comparator<? super K> comparator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public K lastKey() {

		if (this.size() == 0) {
			throw new NoSuchElementException();
		}

		return lastEntry().getKey();
	}

	@Override
	public SortedMap<K, V> headMap(K arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public K firstKey() {

		if (this.size == 0 || root == null) {
			throw new NoSuchElementException();
		}

		TreapNode<K, V> p = root;
		if (p != null) {
			while (p.getLeftChild() != null) {
				p = p.getLeftChild();
			}
		}
		return p.getKey();
	}

	@Override
	public SortedMap<K, V> subMap(K arg0, K arg1) {
		return new SubMap<K, V>(this, arg0, arg1);
	}

	protected class SubMap<K, V> extends AbstractMap<K, V> implements SortedMap<K, V> {
		Treap<K, V> sub = new Treap<K, V>();
		K fromKey;
		K toKey;

		public SubMap(Treap<K, V> in, K arg0, K arg1) {
			if (sub.compare(arg0, arg1) > 0)
				throw new IllegalArgumentException("fromKey > toKey");
			sub = in;
			fromKey = arg0;
			toKey = arg1;
		}

		public SubMap(SubMap<K, V> other) {
			fromKey = other.fromKey;
			toKey = other.toKey;
		}

		private boolean tooLow(Object key) {
			int c = sub.compare(key, fromKey);
			if (c < 0) {
				return true;
			}
			return false;
		}

		private boolean tooHigh(Object key) {
			int c = sub.compare(key, toKey);
			if (c >= 0) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			String result = "{";

			boolean start = false;
			for (Entry c : Treap.this.entrySet()) {

				TreapNode r = (TreapNode) c;

				if (inRange(c.getKey())) {
					start = true;
				}

				if (start) {

					if (inRange(c.getKey())) {
						result = result + c.toString();

						if (TreapNode.successor(r) != null && inRange(TreapNode.successor(r).getKey())) {
							result = result + ", ";
						}
					}

				}
			}

			return result + "}";
		}

		public int hashCode() {
			int result = 0;
			boolean start = false;
			for (Entry c : Treap.this.entrySet()) {

				TreapNode r = (TreapNode) c;

				if (inRange(c.getKey())) {
					start = true;
				}

				if (start) {
					if (inRange(c.getKey())) {
						result = result + r.hashCode();
					}
				}
			}

			return result;
		}

		public boolean equals(Object o) {
			if (o == this)
				return true;

			if (!(o instanceof Map))
				return false;

			Map m = (Map) o;
			// System.out.println("Map size: " +m.size());
			// System.out.println("Treap size: " +this.size());
			if (m.size() == this.size()) {

				for (Object c : m.entrySet()) {
					Entry e = (Entry) c;
					if (inRange(e.getKey()) && !this.entrySet().contains(e)) {
						return false;
					}

				}
				return true;
			} else {
				return false;
			}
		}

		public int size() {
			Set toCount = sub.entrySet();
			int result = 0;
			for (Object c : toCount) {
				Entry e = (Entry) c;
				if (inRange(e.getKey()))
					result++;
			}
			return result;
		}

		private boolean inRange(Object key) {
			return !tooLow(key) && !tooHigh(key);
		}

		@Override
		public V put(K key, V value) {
			if (!inRange(key)) {
				throw new IllegalArgumentException("key out of range");
			}

			return sub.put(key, value);
		}

		@Override
		public V get(Object key) {
			if (key == null)
				throw new NullPointerException();

			if (inRange(key)) {
				return sub.get(key);
			} else {
				throw new NullPointerException();
			}

		}

		@Override
		public Comparator<? super K> comparator() {
			return sub.comparator();
		}

		@Override
		public K firstKey() {

			if (this.size() == 0) {
				throw new NoSuchElementException();
			}

			return firstEntry().getKey();
		}

		@Override
		public SortedMap<K, V> headMap(K arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public K lastKey() {

			if (this.size() == 0) {
				throw new NoSuchElementException();
			}

			return lastEntry().getKey();
		}

		@Override
		public SortedMap<K, V> subMap(K arg0, K arg1) {
			return new SubMap<K, V>(sub, arg0, arg1);
		}

		@Override
		public SortedMap<K, V> tailMap(K arg0) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Set<Map.Entry<K, V>> entrySet() {
			return new SubEntrySet();
		}

		protected class SubEntrySet implements Set<Map.Entry<K, V>> {

			@Override
			public boolean add(java.util.Map.Entry<K, V> o) {
				TreapNode<K, V> me = (TreapNode<K, V>) o;

				if (inRange(me.getKey())) {
					if (SubMap.this.put(me.getKey(), me.getValue()) != null) {
						return true;
					}
				}
				return false;
			}

			public int hashCode() {
				int result = 0;
				boolean start = false;
				for (Entry c : Treap.this.entrySet()) {

					TreapNode r = (TreapNode) c;

					if (inRange(c.getKey())) {
						start = true;
					}

					if (start) {
						if (inRange(c.getKey())) {
							result = result + r.hashCode();
						} else {
							break;
						}
					}
				}

				return result;
			}

			@Override
			public boolean equals(Object o) {
				if (o == null)
					return false;

				if (o == this)
					return true;

				if (!(o instanceof Set))
					return false;

				Set other = (Set) o;

				if (other.size() == this.size()) {

					for (Object c : other) {
						Entry e = (Entry) c;
						if (inRange(e.getKey()) && !this.contains((e))) {
							return false;
						}

					}
					return true;
				} else {
					return false;
				}
			}

			@Override
			public String toString() {
				String result = "[";
				boolean start = false;
				for (Entry c : Treap.this.entrySet()) {

					TreapNode r = (TreapNode) c;

					if (inRange(c.getKey())) {
						start = true;
					}

					if (start) {

						if (inRange(c.getKey())) {
							result = result + c.toString();

							if (TreapNode.successor(r) != null && inRange(TreapNode.successor(r).getKey())) {
								result = result + ", ";
							}
						}

					}
				}

				return result + "]";
			}

			@Override
			public boolean addAll(Collection<? extends java.util.Map.Entry<K, V>> arg0) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void clear() {
				SubMap.this.clear();

			}

			@Override
			public boolean contains(Object o) {
				Map.Entry<K, V> me = (Map.Entry<K, V>) o;
				if (inRange(me.getKey())) {
					return Treap.this.containsKey(me.getKey()) && (me.getValue() == null
							? Treap.this.get(me.getKey()) == null : me.getValue().equals(Treap.this.get(me.getKey())));
				}
				return false;
			}

			@Override
			public boolean containsAll(Collection<?> o) {
				boolean result = true;
				for (Object c : o) {
					Entry e = (Entry) c;
					if (inRange(e.getKey()) && Treap.this.containsKey(e.getKey()) == false
							&& Treap.this.containsValue(e.getValue()) == false) {
						result = false;
					}
				}

				return result;
			}

			@Override
			public boolean isEmpty() {
				return SubMap.this.isEmpty();
			}

			@Override
			public Iterator<java.util.Map.Entry<K, V>> iterator() {
				return new Iterator<java.util.Map.Entry<K, V>>() {
					private int currCount = modCount;
					TreapNode<K, V> lastReturned = null;
					TreapNode<K, V> next = SubMap.this.firstEntry(); // Unsure
																		// if
																		// correct

					@Override
					public boolean hasNext() {

						if (next != null && inRange(next.getKey())) {
							return true;
						}

						return false;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}

					@Override
					public java.util.Map.Entry<K, V> next() {
						TreapNode<K, V> e = next;

						if (e == null)
							throw new NoSuchElementException();

						if (currCount != modCount)
							throw new ConcurrentModificationException();

						// if(e.getKey().equals(toKey)) return null;

						next = TreapNode.successor(e);
						lastReturned = e;
						return e;

					}
				};
			}

			@Override
			public boolean remove(Object o) {
				Map.Entry<K, V> me = (Map.Entry<K, V>) o;

				if (!inRange(me.getKey()))
					return false;

				boolean b = Treap.this.containsKey(me.getKey());
				Treap.this.remove(me.getKey());
				return b;
			}

			@Override
			public boolean removeAll(Collection<?> arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public boolean retainAll(Collection<?> arg0) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public int size() {
				return SubMap.this.size();
			}

			@Override
			public Object[] toArray() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public <T> T[] toArray(T[] a) {
				// TODO Auto-generated method stub
				return null;
			}

		}

		public TreapNode<K, V> firstEntry() {
			Set holder = sub.entrySet();

			for (Object c : holder) {
				return (TreapNode<K, V>) c;
			}
			return null;
		}

		public TreapNode<K, V> lastEntry() {

			Set holder = sub.entrySet();
			TreapNode<K, V> result = sub.root;

			for (Object c : holder) {
				Entry e = (Entry) c;

				if (inRange(e.getKey()) && e.getKey() != toKey) {
					result = (TreapNode<K, V>) e;
				}

			}
			return result;
		}
		/*
		 * TreapNode<K, V> p = (TreapNode<K, V>) Treap.this.root;
		 * 
		 * System.out.println("Root: " +p);
		 * 
		 * TreapNode<K,V> result = p; if (p != null) { while (p.getRightChild()
		 * != null) { p = p.getRightChild();
		 * 
		 * if(inRange(p.getKey())){ result = p; } } }
		 * 
		 * return result;
		 */

	}

	@Override
	public SortedMap<K, V> tailMap(K arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		return new EntrySet();
	}

	protected class EntrySet implements Set<Map.Entry<K, V>> {

		@Override
		public boolean remove(Object o) {
			Map.Entry<K, V> me = (Map.Entry<K, V>) o;

			boolean b = Treap.this.containsKey(me.getKey());
			Treap.this.remove(me.getKey());
			return b;
		}

		@Override
		public boolean contains(Object o) {
			Map.Entry<K, V> me = (Map.Entry<K, V>) o;
			return Treap.this.containsKey(me.getKey()) && (me.getValue() == null ? Treap.this.get(me.getKey()) == null
					: me.getValue().equals(Treap.this.get(me.getKey())));
		}

		@Override
		public int hashCode() {

			int result = 0;

			for (Entry c : this) {
				result = result + c.hashCode();
			}
			return result;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null)
				return false;

			if (o == this)
				return true;

			if (!(o instanceof Set))
				return false;

			Set other = (Set) o;

			if (other.size() == this.size()) {

				return this.containsAll(other);

			} else {
				return false;
			}

			/*
			 * if (o == null) { return false; }
			 * 
			 * if (!(o instanceof Set)) { return false; }
			 * 
			 * if (o instanceof Set) { Set other = (Set) o;
			 * 
			 * for (Entry c : this) { if (!other.contains(c)) { return false; }
			 * }
			 * 
			 * } return true;
			 */
		}

		@Override
		public String toString() {
			String result = "[";
			for (Entry c : this) {
				result = result + c.toString();
				TreapNode r = (TreapNode) c;

				if (TreapNode.successor(r) != null) {
					result = result + ", ";
				}
			}

			return result + "]";
		}

		@Override
		public boolean add(Map.Entry<K, V> o) {
			Map.Entry<K, V> me = o;

			if (Treap.this.put(me.getKey(), me.getValue()) != null) {
				return true;
			}
			return false;
		}

		@Override
		public void clear() {
			modCount++;
			Treap.this.root = null;
			Treap.this.size = 0;

		}

		@Override
		public boolean containsAll(Collection<?> o) {
			boolean result = true;
			for (Object c : o) {
				Entry e = (Entry) c;
				if (Treap.this.containsKey(e.getKey()) == false && Treap.this.containsValue(e.getValue()) == false) {
					result = false;
				}
			}

			return result;
		}

		@Override
		public boolean isEmpty() {
			if (Treap.this.size() == 0)
				return true;
			return false;
		}

		@Override
		public Iterator<java.util.Map.Entry<K, V>> iterator() {
			return new Iterator<java.util.Map.Entry<K, V>>() {
				private int currCount = modCount;
				TreapNode<K, V> lastReturned = null;
				TreapNode<K, V> next = Treap.this.firstEntry();

				@Override
				public boolean hasNext() {
					return next != null;
				}

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public java.util.Map.Entry<K, V> next() {
					TreapNode<K, V> e = next;

					if (e == null)
						throw new NoSuchElementException();

					if (currCount != modCount)
						throw new ConcurrentModificationException();

					next = TreapNode.successor(e);
					lastReturned = e;
					return e;

				}
			};
		}

		@Override
		public boolean removeAll(Collection<?> o) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public int size() {
			return Treap.this.size();
		}

		@Override
		public Object[] toArray() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object[] toArray(Object[] arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean addAll(Collection<? extends java.util.Map.Entry<K, V>> c) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	@Override
	public Collection<V> values() {
		throw new UnsupportedOperationException();
	}

	protected TreapNode<K, V> firstEntry() {
		TreapNode<K, V> p = root;
		if (p != null) {
			while (p.getLeftChild() != null) {
				p = p.getLeftChild();
			}
		}
		return p;
	}

	protected TreapNode<K, V> lastEntry() {
		TreapNode<K, V> p = root;
		if (p != null) {
			while (p.getRightChild() != null) {
				p = p.getRightChild();
			}
		}
		return p;
	}

	@Override
	public Set<K> keySet() {
		throw new UnsupportedOperationException();
	}

	public TreapNode<K, V> getRoot() {
		if (root == null) {
			return null;
		}
		return root;

	}

	public void printThing() {
		root.printThing();
	}
}
