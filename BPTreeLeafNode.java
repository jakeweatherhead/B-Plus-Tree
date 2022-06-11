/**
 * A B+ tree leaf node
 * @param <TKey> the data type of the key
 * @param <TValue> the data type of the value
 */
@SuppressWarnings("unchecked")
class BPTreeLeafNode<TKey extends Comparable<TKey>, TValue> extends BPTreeNode<TKey, TValue> {
	
	protected Object[] values;

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// constructor
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public BPTreeLeafNode(int order) {
		this.m = order;
		this.keys = new Object[m];
		this.values = new Object[m];
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// getValue
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public TValue getValue(int index) {
		return (TValue)this.values[index];
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// setValue
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void setValue(int index, TValue value) {
		this.values[index] = value;
	}

///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// isLeaf
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public boolean isLeaf() {
		return true;
	}


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// insert
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public BPTreeNode<TKey, TValue> insert(TKey key, TValue value) {

    if (this.keyTally < m - 1) {  // node not full
      for (int i = 0; i < m; i++) {
        if (this.keys[i] == null) {
          this.keys[i] = key;
          this.values[i] = value;
          this.keyTally++;
          this.sortNode();
          return this; // nothing to send up so we send this for reference in case it is root
        }
      }
    }
    this.keys[this.keyTally] = key;  // node is full
    this.values[this.keyTally++] = value;
    this.sortNode();
    BPTreeNode<TKey, TValue> upBoundPacket = this.splitNode();
    return upBoundPacket;
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// delete
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Deletes the given key from this leaf node. Note that upboundPacket, 
   * and its configuration, is the way in which case information is
   * communicated with higher levels of the tree.
   * @param key - used to identify item to be deleted.
   */

  public BPTreeNode<TKey, TValue> delete(TKey key) {

    // Find index of key in this node
    Boolean keyInNode = false;
    int indexOfTarget = 0;
    for (int i = 0; i < this.keyTally; i++) {
      if (key.equals((TKey)this.keys[i])) {
        keyInNode = true;
        indexOfTarget = i;
        break;
      }
    }

    // Key not found in node!
    if (!keyInNode) { 
      return this;
    }
    
    // One more insertion and this node will underflow
    if (this.keyTally <= Math.ceil((this.m - 1) / 2)) {

      this.keys[indexOfTarget] = null;
      this.values[indexOfTarget] = null;
      this.keyTally--;
      this.sortNodeAfterDelete(indexOfTarget);
      int numKeysNeeded = (int)Math.ceil((this.m - 1) / 2) - this.keyTally;
      int minKeysForShare = (int)Math.ceil((this.m - 1) / 2) * 2; // Any less and we'll need to merge
      BPTreeLeafNode<TKey,TValue> leftSibling = (BPTreeLeafNode<TKey, TValue>)this.leftSibling;
      BPTreeLeafNode<TKey,TValue> rightSibling = (BPTreeLeafNode<TKey, TValue>)this.rightSibling;

      // Redistribute data from leftSibling to this leaf node
      if (this.leftSibling != null && this.keyTally + this.leftSibling.keyTally >= minKeysForShare) {

        // Share keys of leftSibling with this leaf node
        for (int i = this.leftSibling.keyTally - numKeysNeeded; i < this.leftSibling.keyTally; i++) {
          this.keys[this.keyTally] = leftSibling.keys[i];
          leftSibling.keys[i] = null;
          this.values[this.keyTally++] = leftSibling.values[i];
          leftSibling.values[i] = null;
        }

        this.sortNode();
        leftSibling.keyTally -= numKeysNeeded;
        BPTreeInnerNode<TKey, TValue> upBoundPacket = new BPTreeInnerNode<>(this.m);
        upBoundPacket.keys[0] = this.keys[0];
        upBoundPacket.keyTally++;
        return upBoundPacket;

      // Redistribute data from rightSibling to this leaf node
      } else if (this.rightSibling != null && this.keyTally + this.rightSibling.keyTally >= minKeysForShare) { // we can share with right sibling

        // Share keys of rightSibling with this leaf node
        for (int i = 0; i < numKeysNeeded; i++) {
          this.keys[this.keyTally] = rightSibling.keys[i];
          rightSibling.keys[i] = null;
          this.values[this.keyTally++] = rightSibling.values[i];
          rightSibling.values[i] = null;
        }

        rightSibling.keyTally -= numKeysNeeded;
        rightSibling.sortNodeAfterRightShare();
        BPTreeInnerNode<TKey, TValue> upBoundPacket = new BPTreeInnerNode<>(this.m);
        upBoundPacket.keys[1] = this.rightSibling.keys[0];
        upBoundPacket.keyTally++;
        return upBoundPacket;

      // Merge this leaf node with leftSibling
      } else if (leftSibling != null && leftSibling.keyTally + this.keyTally < (this.m - 1)) {

        leftSibling.redistributeKeys(this);
        if (this.leftSibling != null) this.leftSibling.rightSibling = this.rightSibling;
        if (this.rightSibling != null) this.rightSibling.leftSibling = this.leftSibling;
        BPTreeInnerNode<TKey, TValue> upBoundPacket = new BPTreeInnerNode<TKey, TValue>(this.m);
        upBoundPacket.references[0] = this.leftSibling;  // we will match these references in the index layer so we know what separator to delete

        if (this.rightSibling == null && leftSibling.leftSibling == null) {
          BPTreeLeafNode<TKey, TValue> newUpBoundPacket = new BPTreeLeafNode<>(this.m);
          for (int i = 0; i < leftSibling.keyTally; i++) {
            newUpBoundPacket.keys[newUpBoundPacket.keyTally] = leftSibling.keys[i];
            newUpBoundPacket.values[newUpBoundPacket.keyTally++] = leftSibling.values[i];
          }
          return newUpBoundPacket;
        }

        return upBoundPacket; // all keys will be null if we send a packet up after a merge

      // Merge this leaf node with rightSibling
      } else if (rightSibling != null && rightSibling.keyTally + this.keyTally < (this.m - 1)) {

          rightSibling.redistributeKeys(this);
          if (this.rightSibling != null) this.rightSibling.leftSibling = this.leftSibling;
          if (this.leftSibling != null) this.leftSibling.rightSibling = this.rightSibling;
          BPTreeInnerNode<TKey, TValue> upBoundPacket = new BPTreeInnerNode<TKey, TValue>(this.m);
          upBoundPacket.references[1] = this;  // we will match these references in the index layer so we know which separator to delete

          if (this.leftSibling == null && rightSibling.rightSibling == null) {
            BPTreeLeafNode<TKey, TValue> newUpBoundPacket = new BPTreeLeafNode<>(this.m);
            for (int i = 0; i < rightSibling.keyTally; i++) {
              newUpBoundPacket.keys[newUpBoundPacket.keyTally] = rightSibling.keys[i];
              newUpBoundPacket.values[newUpBoundPacket.keyTally++] = rightSibling.values[i];
            }
            return newUpBoundPacket;
          }
  
          return upBoundPacket; // all keys will be null if we send a packet up from a merge

      // Package node for merge with parent
      } else {

        BPTreeInnerNode<TKey, TValue> upBoundPacket = new BPTreeInnerNode<TKey, TValue>(this.m);

        if (this.leftSibling != null) {
          for (int i = 0; i < leftSibling.keyTally; i++) {
            this.keys[this.keyTally] = this.leftSibling.keys[i];
            this.values[this.keyTally++] = leftSibling.values[i];
          }
          return upBoundPacket;
        }

        // Bundle with right
        if (this.rightSibling != null) {
          for (int i = 0; i < rightSibling.keyTally; i++) {
            this.keys[this.keyTally] = this.rightSibling.keys[i];
            this.values[this.keyTally++] = rightSibling.values[i];
          }
          return upBoundPacket;
        }

        // Delete key from only node in tree
        this.sortNode();
        return this;

      }
    }

    this.keys[indexOfTarget] = null;
    this.values[indexOfTarget] = null;
    this.keyTally--;
    this.sortNodeAfterDelete(indexOfTarget);
    return this;  // Standard leaf delete occurred, no ripple effect
  }


  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
  // values
  ///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public TValue[] values() {

    // Get num values in sequence set
    int numValuesInTree = 0;
    BPTreeLeafNode<TKey, TValue> nodePtr = this;
    while (nodePtr != null) {
      for (int i = 0; i < nodePtr.keyTally; i++) {
        numValuesInTree++;
      }
      nodePtr = (BPTreeLeafNode<TKey, TValue>)nodePtr.rightSibling;
    }
    Object[] tvals = new Object[numValuesInTree];
    
    // Record values in sequence set, ascending order
    nodePtr = this;
    int index = 0;
    while (nodePtr != null) {
      for (int i = 0; i < nodePtr.keyTally; i++) {
        tvals[index++] = nodePtr.values[i];
      }
      nodePtr = (BPTreeLeafNode<TKey, TValue>)nodePtr.rightSibling;
    }
    return (TValue[])tvals;
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// sortNode (Selection sort)
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public void sortNode() {
    for (int i = 0; i < this.keyTally - 1; i++) {
      int minIndex = i;
      for (int j = i + 1; j < this.keyTally; j++) {
          TKey jkey = (TKey)this.keys[j];
          TKey ikey = (TKey)this.keys[i];
          if (jkey.compareTo(ikey) < 0) minIndex = j;
      }
      // Swap keys
      Object temp = this.keys[minIndex];
      this.keys[minIndex] = this.keys[i];
      this.keys[i] = temp;

      // Swap keys
      Object tempVal = this.values[minIndex];
      this.values[minIndex] = this.values[i];
      this.values[i] = tempVal;
    }
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// splitNode
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public BPTreeNode<TKey, TValue> splitNode() {

    BPTreeInnerNode<TKey, TValue> upBoundPacket = new BPTreeInnerNode<>(this.m);
    BPTreeLeafNode<TKey, TValue> newNode = new BPTreeLeafNode<>(this.m);
    int numKeysRemoved = 0;

    for (int i = (int)Math.floor(this.m / 2); i < this.keyTally; i++) {

      newNode.keys[newNode.keyTally] = this.keys[i]; // add data to newNode
      newNode.values[newNode.keyTally++] = this.values[i];
      this.keys[i] = null; // delete split data from original
      this.values[i] = null;
      numKeysRemoved++;
    }
    this.keyTally -= numKeysRemoved;

    // Relink siblings
    newNode.rightSibling = this.rightSibling;
    newNode.leftSibling = this;
    if (newNode.rightSibling != null) newNode.rightSibling.leftSibling = newNode;
    if (newNode.leftSibling != null) newNode.leftSibling.rightSibling = newNode;

    // Prepare upBoundPacket
    upBoundPacket.keys[0] = newNode.keys[0];
    upBoundPacket.references[0] = this;
    upBoundPacket.references[1] = newNode;
    upBoundPacket.keyTally++;
    return upBoundPacket;
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// sortNodeAfterDelete
///////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /**
   * Shifts data right of the indexOfDelete one space to the left.
   * @param indexOfDelete - index in node where deletion occurred
   */

  public void sortNodeAfterDelete(int indexOfDelete) {
    for (int i = indexOfDelete; i < this.m - 1; i++) {
      this.keys[i] = this.keys[i + 1];
      this.values[i] = this.values[i + 1];
    }
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// sortNodeAfterShare
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public void sortNodeAfterRightShare() {
    while (this.keys[0] == null) {
      for (int j = 0; j < this.m - 1; j++) {
        this.keys[j] = this.keys[j + 1];
        this.values[j] = this.values[j + 1];
      }
    }
  }


///////////////////////////////////////////////////////////////////////////////////////////////////////////////
// redistributeKeys
///////////////////////////////////////////////////////////////////////////////////////////////////////////////


  public void redistributeKeys(BPTreeLeafNode<TKey, TValue> underflowNode) {
     int index = 0;
     for (int i = 0; i < underflowNode.keyTally; i++) {
       this.keys[this.keyTally] = underflowNode.keys[index];
       this.values[this.keyTally++] = underflowNode.values[index++];
     }
     this.sortNode();
  }
}
