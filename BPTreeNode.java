/**
 * A B+ tree generic node
 * Abstract class with common methods and data. Each node type implements this
 * class.
 * 
 * @param <TKey>   the data type of the key
 * @param <TValue> the data type of the value
 */
abstract class BPTreeNode<TKey extends Comparable<TKey>, TValue> {

  protected Object[] keys;
  protected int keyTally;
  protected int m;
  protected BPTreeNode<TKey, TValue> parentNode;
  protected BPTreeNode<TKey, TValue> leftSibling;
  protected BPTreeNode<TKey, TValue> rightSibling;
  protected static int level = 0;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// constructor
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected BPTreeNode() {
    this.keyTally = 0;
    this.parentNode = null;
    this.leftSibling = null;
    this.rightSibling = null;
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// getKeyCount
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public int getKeyCount() {
    return this.keyTally;
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// getKey
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @SuppressWarnings("unchecked")
  public TKey getKey(int index) {
    return (TKey) this.keys[index];
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// setKey
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public void setKey(int index, TKey key) {
    this.keys[index] = key;
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// getParent
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public BPTreeNode<TKey, TValue> getParent() {
    return this.parentNode;
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// setParent
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public void setParent(BPTreeNode<TKey, TValue> parent) {
    this.parentNode = parent;
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// isLeaf (abstract)
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public abstract boolean isLeaf();

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// print
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev Print all nodes in a subtree rooted with this node
   * @param node
   */

  @SuppressWarnings("unchecked")
  public void print(BPTreeNode<TKey, TValue> node) {
    level++;
    if (node != null) {
      System.out.print("Level " + level + " ");
      node.printKeys();
      System.out.println();

      // If node is not a leaf, then print all the subtrees rooted with this node.
      if (!node.isLeaf()) {
        BPTreeInnerNode<TKey, TValue> inner = (BPTreeInnerNode<TKey, TValue>) node;
        for (int j = 0; j < (node.m); j++) {
          this.print((BPTreeNode<TKey, TValue>) inner.references[j]);
        }
      }
    }
    level--;
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// printKeys
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev Print all the keys in this node
   */

  protected void printKeys() {
    System.out.print("[");
    for (int i = 0; i < this.getKeyCount(); i++) {
      System.out.print(" " + this.keys[i]);
    }
    System.out.print("]");
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// search
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev Search for key in the tree and return its associated value using 
   * the index set. If the given key is not found, null should be returned.
   */

  public TValue search(TKey key) {
    return this.search(key);
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// insert
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev Insert a new key and its associated value into the B+ tree.
   * The root node of the changed tree should be returned.
   */

  public BPTreeNode<TKey, TValue> insert(TKey key, TValue value) {

    BPTreeNode<TKey, TValue> newRoot = this.insert(key, value);
    if (newRoot != null)
      return newRoot;
    return this;
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// delete
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev Delete a key and its associated value from the B+ tree.
   * The root node of the changed tree should be returned.
   */

  public BPTreeNode<TKey, TValue> delete(TKey key) {
    BPTreeNode<TKey, TValue> newRoot = this.delete(key);
    if (newRoot != null)
      return newRoot;
    return this;
  }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// values
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev Return all associated key values on the B+ tree in ascending key order
   * using the sequence set. An array of the key values should be returned.
   */

  public TValue[] values() {
    return this.values();
  }
}