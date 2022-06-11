/**
 * A B+ tree internal node
 * @param <TKey> the data type of the key
 * @param <TValue> the data type of the value
 */
@SuppressWarnings("unchecked")
class BPTreeInnerNode<TKey extends Comparable<TKey>, TValue> extends BPTreeNode<TKey, TValue> {
	
	protected Object[] references;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// constructor
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	public BPTreeInnerNode(int order) {
		this.m = order;
		// The strategy used here first inserts and then checks for overflow,
		// so an extra space is required in case the node is technically already full.
		this.keys = new Object[m];
		this.references = new Object[m+1];
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// getChild
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	public BPTreeNode<TKey, TValue> getChild(int index) {
		return (BPTreeNode<TKey, TValue>)this.references[index];
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// setChild
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public void setChild(int index, BPTreeNode<TKey, TValue> child) {
		this.references[index] = child;
		if (child != null) {
			child.setParent(this);
    }
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// isLeaf
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	@Override
	public boolean isLeaf() {
		return false;
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// insert
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev Throughout the program, there is the upBoundPacket object.
   * It is is used to convey information to higher levels of the 
   * B+ Tree whenever a change has been made in a lower level that
   * will affect the structure of the tree higher up. If a change 
   * is made in a lower level that will not affect a higher level, 
   * the current node is returned.
   */

  public BPTreeNode<TKey, TValue> insert(TKey key, TValue value) {

    // Find correct child to traverse
    int index = 0;
    Boolean greaterKeyFound = false;
    for (int i = 0; i < this.keyTally; i++) {
        index = i;
        if (key.compareTo((TKey)this.keys[i]) < 0) {
            greaterKeyFound = true;
            break;
        }
    }

    BPTreeNode<TKey, TValue> nextChild = null;
    if (greaterKeyFound) nextChild = (BPTreeNode<TKey, TValue>)this.references[index];
    if (!greaterKeyFound) nextChild = (BPTreeNode<TKey, TValue>)this.references[index + 1];

    // Go down to next child in tree
    BPTreeNode<TKey, TValue> upBoundPacket = nextChild.insert(key, value);

    // A middle key was sent up to this level, add it to this node
    if (upBoundPacket.keys[1] == null && upBoundPacket != nextChild) {
      this.keys[this.keyTally++] = upBoundPacket.keys[0];
      this.sortKeys();
      this.addNewReference((BPTreeInnerNode<TKey, TValue>)upBoundPacket,
                            this.getIndexOfKey((TKey)upBoundPacket.keys[0]));
    }

    // This node is full after insertion
    if (this.keyTally == this.m) {
      return this.splitNode();
    }

    // This node is not full after insertion
    this.linkParentToChild();
    return this;
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// delete
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev Deletion always occurs at the leaf-level in a B+ Tree.
   * This deletion function, first, aims to get down to the
   * sequence set, and then processes any up-bound information
   * that might affect the structure of index set.
   */

  public BPTreeNode<TKey, TValue> delete(TKey key) {

    // Find correct child to traverse
    int index = 0;
    Boolean greaterKeyFound = false;
    for (int i = 0; i < this.keyTally; i++) {
        index = i;
        if (key.compareTo((TKey)this.keys[i]) < 0) {
            greaterKeyFound = true;
            break;
        }
    }

    BPTreeNode<TKey, TValue> nextChild = null;
    if (greaterKeyFound) nextChild = (BPTreeNode<TKey, TValue>)this.references[index];
    if (!greaterKeyFound) nextChild = (BPTreeNode<TKey, TValue>)this.references[index + 1];
    BPTreeNode<TKey, TValue> upBoundPacket = null;

    // Go down to next child in tree
    if (nextChild != null) upBoundPacket = nextChild.delete(key);

     // Special deletion case has occurred
    if (upBoundPacket != nextChild) {

      // Keys were shared after deletion
      if (upBoundPacket.keyTally == 1 && !upBoundPacket.isLeaf()) {

        // Left leaf shared data with right leaf
        if (upBoundPacket.keys[0] != null) {
          if (!greaterKeyFound) index += 1;
          this.keys[index - 1] = upBoundPacket.keys[0];
          return this;
        }
        
        // Right leaf shared data with left leaf
        this.keys[index] = upBoundPacket.keys[1];
        return this;

      } else if (upBoundPacket.keyTally == 1 && upBoundPacket.isLeaf()) {
        return upBoundPacket;
      }

      // Convert to access references
      BPTreeInnerNode<TKey, TValue> upBoundPacket2 = null;
      if (!upBoundPacket.isLeaf()) upBoundPacket2 = (BPTreeInnerNode<TKey, TValue>)upBoundPacket;

      // 
      if (upBoundPacket2 != null && upBoundPacket2.references[0] != null) { // Left merge occurred
        this.deleteSeparatorAfterLeftLeafMerge((BPTreeInnerNode<TKey, TValue>)upBoundPacket2);
        return this;
      } else if (upBoundPacket2 != null && upBoundPacket2.references[1] != null) { // Right merge occurred
        this.deleteSeparatorAfterRightLeafMerge((BPTreeInnerNode<TKey, TValue>)upBoundPacket2);
        return this;
      } else {  // Special case where leaf merges with inner
        return upBoundPacket;
      }
    }
    return this;
  }
  

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// values
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  

  public TValue[] values() {
    TValue[] tvals;
    BPTreeNode<TKey, TValue> nextChild = null;
    nextChild = (BPTreeNode<TKey, TValue>)this.references[0];
    tvals = nextChild.values();
    return tvals;
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// sortKeys (Selection sort)
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public void sortKeys() {
    for (int i = 0; i < this.keyTally - 1; i++) {
      int minIndex = i;
      for (int j = i + 1; j < this.keyTally; j++) {
          TKey jkey = (TKey)this.keys[j];
          TKey ikey = (TKey)this.keys[i];
          if (jkey.compareTo(ikey) < 0) {
            minIndex = j;
          }
      }
      // Swap keys
      Object temp = this.keys[minIndex];
      this.keys[minIndex] = this.keys[i];
      this.keys[i] = temp;
    }
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// getIndexOfKey
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public int getIndexOfKey(TKey key) {
    for (int i = 0; i < this.keyTally; i++) {
      TKey ikey = (TKey)this.keys[i];
      if (ikey.equals(key)) return i;
    }
    return 0;
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// splitNode
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public BPTreeNode<TKey, TValue> splitNode() { 

    BPTreeInnerNode<TKey, TValue> upBoundPacket = new BPTreeInnerNode<TKey, TValue>(this.m);
    BPTreeInnerNode<TKey, TValue> newNode = new BPTreeInnerNode<TKey, TValue>(this.m);

    // Prepare middle key to be sent to higher level
    int middleIndex = (int)Math.floor(this.m / 2);
    upBoundPacket.keys[0] = this.keys[middleIndex];
    upBoundPacket.keyTally++;
    this.keys[middleIndex] = null;
    this.keyTally--;
    int keysRemoved = 0;
    
    // Fill the references of newNode
    // Simultaneoulsy delete them from pre-split node
    int index = 0;
    for (int i = middleIndex + 1; i <= this.m; i++) {
        newNode.references[index++] = this.references[i];
        this.references[i] = null;
    }

    // Fill the keys of newNode
    // Simultaneoulsy delete them from pre-split node
    for (int j = middleIndex + 1; j < this.m; j++) {
        newNode.keys[newNode.keyTally++] = this.keys[j];
        this.keys[j] = null;
        keysRemoved++;
    }

    this.keyTally -= keysRemoved;
    upBoundPacket.references[0] = this;
    upBoundPacket.references[1] = newNode;
    this.linkParentToChild();
    newNode.linkParentToChild();
    return upBoundPacket;
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// addNewReference
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public void addNewReference(BPTreeInnerNode<TKey, TValue> upBoundPacket, int indexOfNewKey) {
    for (int i = this.m; i >= 0; i--) {
      if (i == indexOfNewKey + 1) {
        this.references[i] = upBoundPacket.references[1];
        return;
      }
      this.references[i] = this.references[i - 1];
    } 
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// linkHierachy
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public void linkParentToChild() {
    for (int i = 0; i <= this.keyTally; i++) {
      this.setChild(i, this.getChild(i));
    }
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// sortKeysAfterDelete
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public void sortKeysAfterDelete(int indexOfDelete) {
    for (int i = indexOfDelete; i < this.m - 1; i++) {
      this.keys[i] = this.keys[i + 1];
    }
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// sortReferencesAfterDelete
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public void sortReferencesAfterDelete(int indexOfDelete) {
    for (int i = indexOfDelete; i < this.m - 1; i++) {
      this.references[i] = this.references[i + 1];
    }
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// deleteSeparatorAfterLeftLeafMerge
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev A merge has occurred with a leaf node and its left sibling.
   * @dev This function deletes the stale, leftover separator key in the index set.
   * @param referenceNode - information node used to identify stale separator.
   */

  public void deleteSeparatorAfterLeftLeafMerge(BPTreeInnerNode<TKey, TValue> referenceNode) {
    for (int i = 0; i < this.keyTally + 1; i++) {  // looping through references
      if (this.references[i] == referenceNode.references[0]) {
        this.keys[i] = null;
        this.keyTally--;
        this.references[i + 1] = null;
        this.sortKeysAfterDelete(i);
        this.sortReferencesAfterDelete(i + 1);
      }
    }
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// deleteSeparatorAfterRightLeafMerge
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * @dev A merge has occurred with a leaf node and its right sibling.
   * @dev This function deletes the stale, leftover separator key in the index set.
   * @param referenceNode - information node used to identify stale separator.
   */

  public void deleteSeparatorAfterRightLeafMerge(BPTreeInnerNode<TKey, TValue> referenceNode) {
    for (int i = 0; i < this.keyTally + 1; i++) {
      if (this.references[i] == referenceNode.references[1]) {
        this.keys[i] = null;
        this.keyTally--;
        this.references[i] = null;
        this.sortKeysAfterDelete(i);
        this.sortReferencesAfterDelete(i);
      }
    }
  }
}