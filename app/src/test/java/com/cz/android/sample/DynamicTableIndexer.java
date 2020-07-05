package com.cz.android.sample;


import com.cz.android.datastructure.sparse.SparseIntArray;

public class DynamicTableIndexer {
    private final SparseIntArray tableRowArray;
    private final SparseIntArray tableColumnArray;

    public DynamicTableIndexer() {
        tableRowArray =new SparseIntArray();
        tableColumnArray =new SparseIntArray();
    }

    public void clear() {
        tableColumnArray.clear();
        tableRowArray.clear();
    }


    public int findTableCellColumn(float x){
        int index = binarySearchStartIndex(tableColumnArray, x);
        if(0 > index){
            return tableColumnArray.keyAt(0)-1;
        } else if (index < tableColumnArray.size()){
            return tableColumnArray.keyAt(index);
        } else {
            return tableColumnArray.keyAt(tableColumnArray.size()-1)+1;
        }
    }

    public int findTableCellRow(float y){
        // [300,600,900,1200]
        // 0, 1 , 2 , 3 , 4
        int index = binarySearchStartIndex(tableRowArray, y);
        if(0 > index){
            return tableRowArray.keyAt(0)-1;
        } else if (index < tableRowArray.size()){
            return tableRowArray.keyAt(index);
        } else {
            return tableRowArray.keyAt(tableRowArray.size()-1)+1;
        }
    }

    public int getTableCellOffsetX(int column){
        return tableColumnArray.get(column,0);
    }

    public int getTableCellOffsetY(int row){
        return tableRowArray.get(row,0);
    }

    public boolean tableColumnExist(int column){
        return 0 <= tableColumnArray.indexOfKey(column);
    }

    public boolean tableRowExist(int row){
        return 0 <= tableRowArray.indexOfKey(row);
    }

    public int getStartTableColumn(){
        int tableColumn=0;
        if(0 < tableColumnArray.size()){
            tableColumn=tableColumnArray.keyAt(0);
        }
        return tableColumn;
    }

    public int getEndTableColumn(){
        int tableColumn=0;
        int arraySize = tableColumnArray.size();
        if(0 < arraySize){
            tableColumn=tableColumnArray.keyAt(arraySize-1);
        }
        return tableColumn;
    }

    public int getStartTableRow(){
        int tableRow=0;
        if(0 < tableRowArray.size()){
            tableRow=tableRowArray.keyAt(0)-1;
        }
        return tableRow;
    }

    public int getEndTableRow(){
        int tableRow=0;
        int arraySize = tableRowArray.size();
        if(0 < arraySize){
            tableRow=tableRowArray.keyAt(arraySize-1)-1;
        }
        return tableRow;
    }

    public int getStartTableColumnOffset(){
        int startTableColumn = getStartTableColumn();
        return tableColumnArray.get(startTableColumn);
    }

    public int getEndTableColumnOffset(){
        int endTableColumn = getEndTableColumn();
        return tableColumnArray.get(endTableColumn);
    }

    public int getStartTableRowOffset(){
        int startTableRow = getStartTableRow();
        return tableRowArray.get(startTableRow);
    }

    public int getEndTableRowOffset(){
        int endTableRow = getEndTableRow();
        return tableRowArray.get(endTableRow);
    }

    public int getTableColumnSize(int i){
        int previousTableColumnSize = tableColumnArray.get(i-1,0);
        int tableColumnSize = tableColumnArray.get(i,0);
        return tableColumnSize-previousTableColumnSize;
    }

    public int getTableRowSize(int i){
        int previousTableRowSize = tableRowArray.get(i-1,0);
        int tableRowSize = tableRowArray.get(i,0);
        return tableRowSize-previousTableRowSize;
    }

    public void removeTableRow(int from, int to) {
        for(int i=from;i<to;i++){
            tableRowArray.delete(i);
        }
    }

    public void removeTableColumn(int from, int to) {
        for(int i=from;i<to;i++){
            tableColumnArray.delete(i);
        }
    }

    public void addTableColumnFromStart(int i, int size) {
        if(0 == tableColumnArray.size()){
            tableColumnArray.append(0,0);
        }
        int tableColumnSize = tableColumnArray.get(i+1,0);
        tableColumnArray.put(i,tableColumnSize-size);
    }

    public void addTableColumnFromEnd(int i, int size) {
        if(0 == tableColumnArray.size()){
            tableColumnArray.append(0,0);
        }
        int tableColumnSize = tableColumnArray.get(i,0);
        tableColumnArray.put(i+1,tableColumnSize+size);
    }

    public void addTableRow(int i, int size) {
        if(0 == tableRowArray.size()){
            tableRowArray.append(0,0);
        }
        int tableRowSize = tableRowArray.get(i,0);
        tableRowArray.put(i+1,tableRowSize+size);
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

    @Override
    public String toString() {
        return tableColumnArray.toString();
    }
}
