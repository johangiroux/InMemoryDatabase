package com.johan.inmemorydatabase.database;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by johan on 4/27/16.
 */
public class MemoryDatabase {

    private LinkedList<TransactionBlock> blocks;

    /***
     * Database constructor
     * add the first TransitionBlock
     */
    public MemoryDatabase() {
        blocks = new LinkedList<>();
        blocks.add(new TransactionBlock());
    }

    /**
     * set the value for the key
     *
     * @param key
     * @param value
     */
    public void set(String key, Integer value) {
        blocks.getLast().set(key, value);
    }

    /**
     * get the value for the key
     *
     * @param key
     * @return the value, null if no value is set for this key
     */
    public Integer get(String key) {
        return blocks.getLast().get(key);
    }


    /**
     * numEqualTo will browse all the past transactions, commited or not and return the number of
     * key for the value in arg1
     *
     * @param value the value we want the count from
     * @return the number of key for this value, 0 if none
     */
    public Integer numEqualTo(Integer value) {
        return blocks.getLast().numEqualTo(value);
    }


    /**
     * Commit all the pending transactions
     *
     * @return true if committed, false if nothing to commit.
     */
    public boolean commit() {
        // nothing to commit
        if (blocks.size() <= 1) return false;

        HashMap<String, Integer> keyValue = new HashMap<>();
        HashMap<Integer, Integer> valueCount = new HashMap<>();

        // iterate through all the TransactionBlock
        ListIterator<TransactionBlock> iterator = blocks.listIterator();
        while (iterator.hasNext()) {
            TransactionBlock block = iterator.next();
            keyValue.putAll(block.getKeyValue()); // putAll the transaction of this block
        }

        // iterate through all the transactions and re-compute the valueCount
        for (Map.Entry<String, Integer> entry : keyValue.entrySet()) {
            Integer value = entry.getValue();
            if (valueCount.get(value) == null) {
                valueCount.put(value, 1);
            } else {
                valueCount.put(value, valueCount.get(value) + 1);
            }
            keyValue.put(entry.getKey(), entry.getValue());
        }

        blocks.clear();
        // the first TransactionBlock as all the committed Transaction
        blocks.add(new TransactionBlock(keyValue, valueCount));

        return true;
    }


    /**
     * rollBack the previous uncommitted transaction
     *
     * @return true if rollBacked, false otherwise
     */
    public boolean rollBack() {
        // don't remove the 1st one because it contains the committed transactions
        if (blocks.size() <= 1) return false;
        blocks.removeLast();
        return true;
    }


    /**
     * begin create a new TransactionBlock, each new TransactionBlock has a previous block, except
     * for the first one representing the committed transactions
     */
    public void begin() {
        TransactionBlock block = new TransactionBlock();
        block.setPreviousBlock(blocks.getLast());
        blocks.add(block);
    }


    public static void main(String[] args) {
        MemoryDatabase memoryDatabase = new MemoryDatabase();
        Scanner scanner = new Scanner(System.in);
        scanner.useDelimiter("\\s+"); // space
        String line;
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            String[] params = line.split("\\s+"); // space
            String command = params[0];
            String key;
            Integer value;
            try {
                switch (command) {
                    case "GET":
                        key = params[1];
                        // if value for key exist print value, else print NULL
                        System.out.println(memoryDatabase.get(key) != null ? memoryDatabase.get(key) : "NULL");
                        break;
                    case "SET":
                        key = params[1];
                        value = Integer.parseInt(params[2]);
                        memoryDatabase.set(key, value);
                        break;
                    case "UNSET":
                        key = params[1];
                        memoryDatabase.set(key, null);
                        break;
                    case "NUMEQUALTO":
                        value = Integer.parseInt(params[1]);
                        System.out.println(memoryDatabase.numEqualTo(value));
                        break;
                    case "BEGIN":
                        memoryDatabase.begin();
                        break;
                    case "ROLLBACK":
                        // if can't rollback, print NO TRANSACTION
                        if (!memoryDatabase.rollBack()) System.out.println("NO TRANSACTION");
                        break;
                    case "COMMIT":
                        // if nothing to commit, print NO TRANSACTION
                        if (!memoryDatabase.commit()) System.out.println("NO TRANSACTION");
                        break;
                    case "END":
                        return;
                    case "":
                        break;
                    default:
                        System.out.println("Invalid command: " + command);
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid param: " + line + " is not a number");
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Missing param: " + line);
            }
        }
        scanner.close();
    }
}

/**
 * Class representing a block of Transaction, you can access to the history through the
 * previous block, like a Node.
 */
class TransactionBlock {

    private HashMap<String, Integer> keyValue = new HashMap<>(); // key / value
    private HashMap<Integer, Integer> valueCount = new HashMap<>(); // keep count of value for

    private TransactionBlock previousBlock;

    /**
     * Empty TransactionBlock constructor
     *
     */
    public TransactionBlock() {
    }

    /**
     * TransactionBlock constructor
     * @param keyValue
     * @param valueCount
     */
    public TransactionBlock(HashMap<String, Integer> keyValue, HashMap<Integer, Integer> valueCount) {
        this.keyValue = keyValue;
        this.valueCount = valueCount;
    }

    /**
     * set the previous block, it allows the nested transactions
     * @param block
     */
    public void setPreviousBlock(TransactionBlock block) {
        previousBlock = block;
    }

    /**
     * get all the key/value for this block
     * @return keyValue HashMap<String, Integer>
     */
    public HashMap<String, Integer> getKeyValue() {
        return keyValue;
    }


    /***
     * The method set match the command SET
     *
     * @param key   the key (name) of the object
     * @param value the Integer value
     */
    public void set(String key, Integer value) {

        /**
         * if a value is already set for this key, and is different that the current value
         * then we need to decrease the count for that value
         */
        Integer prevValue = get(key);
        if (prevValue != null && !prevValue.equals(value)) {
            Integer prevValueCounter = numEqualTo(prevValue);
            valueCount.put(prevValue, --prevValueCounter);
        }

        /**
         * if there is no value associated with this key, or the value is different than the new
         * value, increment the count
         */
        if (prevValue == null || !prevValue.equals(value)) {
            Integer currentValueCounter = numEqualTo(value);
            if (value != null) {
                if (currentValueCounter != null) {
                    valueCount.put(value, ++currentValueCounter);
                } else {
                    valueCount.put(value, 1);
                }
            }
        }

        keyValue.put(key, value);
    }


    /**
     * get the value relative to the key
     *
     * @param key the key
     * @return value the Integer value of the corresponding String key.
     */
    public Integer get(String key) {
        TransactionBlock block = this;
        Integer value = block.keyValue.get(key);
        while (!block.keyValue.containsKey(key) && block.previousBlock != null) {
            block = block.previousBlock;
            value = block.keyValue.get(key);
        }
        return value;
    }

    /**
     * get through all the past transactions and get the sum of the count corresponding to the key
     *
     * @param value
     * @return the sum of key for the corresponding value.
     */
    public Integer numEqualTo(Integer value) {
        if (value == null) return 0;

        TransactionBlock block = this;
        Integer sum = valueCount.get(value);
        while (sum == null && block.previousBlock != null) {
            block = block.previousBlock;
            sum = block.valueCount.get(value);
        }

        if (sum == null)
            return 0;
        else {
            return sum;
        }
    }
}