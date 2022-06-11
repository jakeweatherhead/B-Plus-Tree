import java.util.ArrayList;
import java.util.Random;

public class Main {

  public static final String REVIEW_PURPLE = "\033[0;35m";
  public static final String ANSI_RESET = "\u001B[0m";

  private static void printInsertOrder(ArrayList<Integer> keySafe) {
    System.out.print("Insert Order = { ");
    for (int i = 0; i < keySafe.size(); i++) {
      if (i == keySafe.size() - 1) {
        System.out.print(keySafe.get(i) + "};");
        break;
      }
      System.out.print(keySafe.get(i) + ",");
    }
  }

  private static void printDeleteOrder(ArrayList<Integer> deleteList) {
    System.out.print("Delete order = { ");
    for (int i = 0; i < deleteList.size(); i++) {
      if (i == deleteList.size() - 1) {
        System.out.print(deleteList.get(i) + "};");
        break;
      }
      System.out.print(deleteList.get(i) + ",");
    }
  }

  private static void test() {

		System.out.println("------------------------------------- Test -------------------------------------");
		BPTree<Integer, Integer> testTree = new BPTree<Integer, Integer>(3);
    ArrayList<Integer> keys = new ArrayList<Integer>();
    ArrayList<Integer> keySafe = new ArrayList<Integer>(); // Stores inserted keys for future reference
    ArrayList<Integer> deleteList = new ArrayList<Integer>();
    ArrayList<Integer> values = new ArrayList<Integer>();
    Random random = new Random();

    ////////////////////////////////////////////////////////
    // Manual Review Section
    ////////////////////////////////////////////////////////

    int[] insertOrder = { 332,172,497,851,925,222,897,95,467,844};
    int[] deleteOrder = { 467,851,172,897,844};//,925,497,222,95,332};

    for (int i = 0; i < insertOrder.length; i++) {
      values.add(i * 2);
      testTree.insert(insertOrder[i], i * 2);
    }

    System.out.println("tree after inserts");
    testTree.print();

    for (int j = 0; j < deleteOrder.length; j++) {
      System.out.println("Delete " + deleteOrder[j]);
      testTree.delete(deleteOrder[j]);
      testTree.print();
    } 

    ////////////////////////////////////////////////////////
    // Automatic Testing Section (Rudimentary Fuzzer)
    ////////////////////////////////////////////////////////

    // Generate random list of keys to insert
    int numInserts = random.nextInt(20);
    for (int j = 0; j < numInserts; j++) {
      int randKey = 1 + (int)(Math.random() * ((1000 - 1) + 1));
      keySafe.add(randKey);
      keys.add(randKey);
    }
    
    // Insert data into tree
    for (int i = 0; i < keys.size(); i++) {
      testTree.insert(keys.get(i), i * 5);
    }

    System.out.println("Tree structure after all inserts");
    testTree.print();

    // Delete data from tree
    while (keys.size() > 0) {
      int deleteIndex = random.nextInt(keys.size());
      int deleteKey = keys.get(deleteIndex);
      keys.remove(deleteIndex);

      // Print next key to be deleted
      System.out.println("Delete " + deleteKey);
      deleteList.add(deleteKey);
      testTree.delete(deleteKey);
      testTree.print();
    }

    // General information about the test case
    System.out.println("\n---------------------------------");
    System.out.println(REVIEW_PURPLE + "General Information" + ANSI_RESET);
    System.out.println("---------------------------------");
    System.out.println("Number of keys inserted: " + numInserts);
    System.out.println("Number of keys deleted: " + deleteList.size());
    printInsertOrder(keySafe);
    printDeleteOrder(deleteList);
    System.out.println("---------------------------------");
	}

  public static void main(String[] args) {
    test();
  }
}
