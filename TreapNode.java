package cmsc420.sortedmap;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.SortedMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class TreapNode<K, V> extends AbstractMap.SimpleEntry<K, V> implements SortedMap.Entry<K, V> {

	private int priority;
	private TreapNode<K, V> parent;
	private TreapNode<K, V> leftChild;
	private TreapNode<K, V> rightChild;
	private boolean empty = true;
	private Comparator<K> comp;

	public TreapNode(K inKey, V inValue, int inPriority, TreapNode<K, V> p, TreapNode<K, V> l, TreapNode<K, V> r) {
		super(inKey, inValue);
		priority = inPriority;
		parent = p;
		leftChild = l;
		rightChild = r;
		empty = false;
		comp = null;

	}

	public TreapNode(K inKey, V inValue, int inPriority, TreapNode<K, V> p, TreapNode<K, V> l, TreapNode<K, V> r,
			Comparator<K> in) {
		super(inKey, inValue);
		priority = inPriority;
		parent = p;
		leftChild = l;
		rightChild = r;
		empty = false;
		comp = in;

	}

	@Override
	public boolean equals(Object o) {
		return super.equals(o);
		/*
		 * if(!(o instanceof Map.Entry)){ return false; }
		 * 
		 * Map.Entry<K, V> other = (Map.Entry<K, V>) o;
		 * 
		 * return this.getKey().equals(other.getKey()) &&
		 * this.getValue().equals(other.getValue());
		 */
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
		/*
		 * int keyHash = (this.getKey() == null ? 0 : this.getKey().hashCode());
		 * int valueHash = (this.getValue() == null ? 0 :
		 * this.getValue().hashCode()); return keyHash ^ valueHash;
		 */
	}

	public V put(TreapNode<K, V> r) {

		int comparedResult;

		if (comp != null) {
			comparedResult = comp.compare(this.getKey(), r.getKey());

			if (comparedResult == 0) {
				this.setValue(r.getValue());
				return r.getValue();
			}

			if (comparedResult < 0) {
				if (this.getRightChild() == null) {
					this.setRight(r);
					r.setParent(this);
					return r.getValue();
				} else {
					return this.getRightChild().put(r);
				}
			}
			if (comparedResult > 0) {

				if (this.getLeftChild() == null) {
					this.setLeft(r);
					r.setParent(this);
					return r.getValue();
				} else {
					return this.getLeftChild().put(r);
				}
			}

		} else {
			Comparable<? super K> k = (Comparable<? super K>) this.getKey();

			comparedResult = k.compareTo(r.getKey());

			if (comparedResult == 0) {
				this.setValue(r.getValue());
				return r.getValue();
			}

			if (comparedResult < 0) {

				if (this.getRightChild() == null) {
					this.setRight(r);
					r.setParent(this);
					return r.getValue();
				} else {
					return this.getRightChild().put(r);
				}
			}
			if (comparedResult > 0) {

				if (this.getLeftChild() == null) {
					this.setLeft(r);
					r.setParent(this);
					return r.getValue();
				} else {
					return this.getLeftChild().put(r);
				}
			}

		}

		// If we fell off the treap, an unsuccessful search, add it as a leaf of
		// the
		// parent.
		return null;
	}

	public String toContinuedString() {
		String result = super.getKey() + "=" + super.getValue();

		if (this.getLeftChild() == null && this.getRightChild() == null) {

			return result;
		}

		if (this.getLeftChild() != null) {
			result = result + ", " + this.getLeftChild().toContinuedString();
		}

		if (this.getRightChild() != null) {
			result = result + ", " + this.getRightChild().toContinuedString();
		}

		return result;

	}

	@Override
	public K getKey() {
		return super.getKey();
	}

	@Override
	public V getValue() {
		return super.getValue();
	}

	public TreapNode<K, V> getParent() {

		return parent;
	}

	public TreapNode<K, V> getLeftChild() {
		if (leftChild == null)
			return null;
		return leftChild;
	}

	public TreapNode<K, V> getRightChild() {
		if (rightChild == null)
			return null;
		return rightChild;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public V setValue(V inValue) {
		return super.setValue(inValue);
	}

	public void setLeft(TreapNode<K, V> in) {
		leftChild = in;
		// if(leftChild != null)leftChild.setParent(this);
	}

	public void setRight(TreapNode<K, V> in) {
		rightChild = in;
		// if(rightChild != null)rightChild.setParent(this);
	}

	public void setParent(TreapNode<K, V> in) {

		if (in == null) {
			parent = null;
		}

		parent = in;
	}

	public String nodeType() {
		return "full";
	}

	public Element printNode(Document results) {

		final Element treapNode = results.createElement("node");

		if (this != null) {
			treapNode.setAttribute("key", getKey().toString());
			treapNode.setAttribute("priority", Integer.toString((this.getPriority())));
		} else {
			return treapNode;
		}
		/*
		 * if(parent == null){ treapNode.setAttribute("parent", "null"); }else{
		 * treapNode.setAttribute("parent", this.getParent().toString()); }
		 */

		treapNode.setAttribute("value", getValue().toString());

		if (leftChild != null) {
			treapNode.appendChild(this.getLeftChild().printNode(results));

		} else {
			final Element emptyNode = results.createElement("emptyChild");
			treapNode.appendChild(emptyNode);
		}

		if (rightChild != null) {
			treapNode.appendChild(this.getRightChild().printNode(results));

		} else {
			final Element emptyNode = results.createElement("emptyChild");
			treapNode.appendChild(emptyNode);
		}
		return treapNode;

	}

	public static TreapNode successor(TreapNode t) {
		if (t == null) {
			return null;
		} else if (t.getRightChild() != null) {
			TreapNode p = t.getRightChild();

			while (p.getLeftChild() != null) {
				p = p.getLeftChild();
			}
			return p;

		} else {
			TreapNode p = t.getParent();
			TreapNode ch = t;

			while (p != null && ch == p.getRightChild()) {
				ch = p;
				p = p.parent;
			}
			return p;
		}

	}

	public static TreapNode predecessor(TreapNode t) {
		if (t == null) {
			return null;
		} else if (t.getLeftChild() != null) {
			TreapNode p = t.getLeftChild();

			while (p.getRightChild() != null) {
				p = p.getRightChild();
			}
			return p;
		} else {
			TreapNode p = t.getParent();
			TreapNode ch = t;

			while (p != null && ch == p.getLeftChild()) {
				ch = p;
				p = p.getParent();
			}
			return p;
		}

	}

	public void printThing() {

		System.out.println("Key: " + this.getKey() + " Value: " + this.getValue() + " Priority: " + this.getPriority());
		if (parent != null) {
			System.out.println("Is the child of: " + this.getParent().toString());
		} else {
			System.out.println("Is the root.");
		}

		System.out.println("Left Child: ");
		if (this.getLeftChild() != null) {
			this.getLeftChild().printThing();
		} else {
			System.out.println("Empty.");
		}

		System.out.println("Right Child: ");
		if (this.getRightChild() != null) {
			this.getRightChild().printThing();
		} else {
			System.out.println("Empty.");
		}

	}

}
