package com.cz.android.sample;

import com.cz.android.datastructure.sparse.SparseIntArray;

import org.junit.Test;

public class BinarySearchTest {

    @Test
    public void tableColumnIndexerTest(){
        DynamicTableIndexer tableIndexer=new DynamicTableIndexer();
        for(int i=0;i<7;i++){
            tableIndexer.addTableColumnFromEnd(i,300);
        }
        System.out.println(tableIndexer.findTableCellColumn(5));
        System.out.println(tableIndexer.findTableCellColumn(833));

        //Fetch table column from location;
        System.out.println(tableIndexer.getTableCellOffsetX(0));
        System.out.println(tableIndexer.getTableCellOffsetX(1));

        System.out.println(tableIndexer.findTableCellColumn(1805));

//        //Test the table column
//        System.out.println("start:"+tableIndexer.getStartTableColumn()+" end:"+tableIndexer.getEndTableColumn());
//        tableIndexer.removeTableColumn(0,1);
//        System.out.println("start:"+tableIndexer.getStartTableColumn()+" end:"+tableIndexer.getEndTableColumn());
//
//        int endTableColumn = tableIndexer.getEndTableColumn();
//        tableIndexer.addTableColumnFromEnd(endTableColumn+1,400);
//        System.out.println(tableIndexer.tableColumnExist(1));
//        System.out.println(tableIndexer.tableColumnExist(2));
    }

    @Test
    public void tableRowIndexerTest(){
        DynamicTableIndexer tableIndexer=new DynamicTableIndexer();
        for(int i=0;i<20;i++){
            tableIndexer.addTableRow(i,300);
        }
        System.out.println(tableIndexer.findTableCellColumn(5));
        System.out.println(tableIndexer.findTableCellColumn(833));

        //Fetch table column from location;
        System.out.println(tableIndexer.getTableCellOffsetX(0));
        System.out.println(tableIndexer.getTableCellOffsetX(1));

        System.out.println(tableIndexer.findTableCellColumn(1805));
    }
    @Test
    public void binarySearchTest(){
        SparseIntArray tableArray=new SparseIntArray();
        int[] array=new int[]{300,1600,900,1200};
        for(int i=0;i<array.length;i++){
            tableArray.append(i,array[i]);
        }
        int i = binarySearchStartIndex(tableArray, 306)+1;
        int key = tableArray.keyAt(i);
        System.out.println("i:"+key+" v:"+tableArray.get(key));
        System.out.println(binarySearchStartIndex(tableArray, 5));
        System.out.println(binarySearchStartIndex(tableArray, 1205));
    }

    private int binarySearchStartIndex(SparseIntArray array, float value){
        int start = 0;
        int result = -1;
        int end = array.size() - 1;
        while (start <= end) {
            int middle = (start + end) / 2;
            int key = array.keyAt(middle);
            int middleValue = array.get(key);
            if (value == middleValue) {
                result = middle;
                break;
            } else if (value < middleValue) {
                end = middle - 1;
            } else {
                start = middle + 1;
            }
        }
        if (-1 == result) {
            result = start-1;
        }
        return result;
    }
}
