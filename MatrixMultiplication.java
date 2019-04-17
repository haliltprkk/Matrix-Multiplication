// It was created by İbrahim Halil TOPRAK
// 05.04.2019
import java.util.ArrayList;

public class MatrixMultiplication {

    //My whole matrices are static because I don't want to send lists as a parameter from somewhere to somewhere.
    //Also I am working with threads so, by making result array static the threads can reach easily and make their tasks on resultMatrix.
    public static int resultMatrix[][];
    public static int matrix1[][];
    public static int matrix2[][];

    public static void main(String[] args) {

        //Basic control
        if (args.length < 5) {
            System.out.println("\n**************************************************\n");
            System.out.println("\n>> 5 numbers are needed\n\n1)First matrix row length\n2)First matrix column length\n3)Second matrix row length\n4)Second matrix column length\n5)Thread count (it can't be greater than cell count of result matrix!)\n");
            System.out.println("\n**************************************************\n");
            System.exit(1);
        }

        //Basic control
        for (String i : args) {
            if (!i.matches("[0-9]+")) {
                System.out.println("\nOnly numeric characters please.\n");
                System.exit(1);
            }
        }

        int matrix1Row = Integer.parseInt(args[0]);
        int matrix1Column = Integer.parseInt(args[1]);
        int matrix2Row = Integer.parseInt(args[2]);
        int matrix2Column = Integer.parseInt(args[3]);
        int threadCount = Integer.parseInt(args[4]);

        matrix1 = assignMatrix(matrix1Row, matrix1Column);
        matrix2 = assignMatrix(matrix2Row, matrix2Column);

        if (matrix1Column != matrix2Row) {
            System.out.println("\nTo make multiply calculation between two matrices first matrix column length should be equals to second matrix row length.\n");
            System.exit(1);
        }

        //it is matrix 1
        System.out.println("-- It is matrix 1 ------------------------------\n");
        print(matrix1);

        //it is matrix 2
        System.out.println("-- It is matrix 2 ------------------------------\n");
        print(matrix2);

        //this is the matrix that keep the result
        resultMatrix = new int[matrix1Row][matrix2Column];
        int cellCounts = resultMatrix.length * resultMatrix[0].length;

        //Basic control
        if (threadCount > cellCounts) {
            System.out.println("MyThread count can not be greater than cell count!!!");
            System.exit(1);
        }

        //this is the logic part of the algorithm
        ArrayList<Cell> cellArrayList = new ArrayList<>();
        ArrayList<MyThread> threadArrayList = new ArrayList<>();

        //There are two situations cell count can be divided by threadCount exactly or not, first case is more easy than second one.
        //I am arranging counter that shows workload for per thread, and I am iterating them with simple for loop.
        if (cellCounts % threadCount == 0) {
            int counter = cellCounts / threadCount;
            for (int i = 0; i < resultMatrix.length; i++) {
                for (int j = 0; j < resultMatrix[0].length; j++) {
                    if (counter > 0) {
                        if (cellArrayList == null)
                            cellArrayList = new ArrayList<>();
                        cellArrayList.add(new Cell(i, j));
                        if (--counter <= 0) {
                            MyThread myThread = new MyThread(cellArrayList);
                            myThread.run();
                            threadArrayList.add(myThread);
                            counter = cellCounts / threadCount;
                            cellArrayList = null;
                        }
                    }
                }
            }
        }

        /*I am arranging counter that shows workload for per thread same like first case
         * but for this case there is some cells that residue from dividing process,
         * I am shared out those cells to threads evenly.
         * */
        else {
            int counter = cellCounts / threadCount;
            int residue = cellCounts % threadCount;
            for (int i = 0; i < resultMatrix.length; i++) {
                for (int j = 0; j < resultMatrix[0].length; j++) {
                    if (counter > 0) {
                        if (cellArrayList == null)
                            cellArrayList = new ArrayList<>();
                        cellArrayList.add(new Cell(i, j));
                        if (--counter == 0 && residue == 0) {
                            MyThread myThread = new MyThread(cellArrayList);
                            myThread.run();
                            threadArrayList.add(myThread);
                            counter = cellCounts / threadCount;
                            cellArrayList = null;
                        }
                    } else if (counter == 0 && residue != 0) {
                        residue--;
                        cellArrayList.add(new Cell(i, j));
                        MyThread myThread = new MyThread(cellArrayList);
                        myThread.run();
                        threadArrayList.add(myThread);
                        counter = cellCounts / threadCount;
                        cellArrayList = null;
                    }
                }
            }

        }

        //By this loop, it is suspended until whole threads has been terminated
        for (Thread i : threadArrayList) {
            try {
                i.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //it is resultMatrix with normal calculation,You can check the result with it.
        //System.out.println("-- It is resultMatrix with normal calculation ------------------------------\n");
        //multiplyMatrices();

        //it is resultMatrix
        System.out.println("-- It is resultMatrix ------------------------------\n");
        print(resultMatrix);

    }

    public static int[][] assignMatrix(int row, int column) {
        int array[][] = new int[row][column];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < column; j++) {
                array[i][j] = (int) ((Math.random() + 1) * 2);
            }
        }
        return array;
    }

    public static void multiplyMatrices() {
        int[][] product = new int[matrix1.length][matrix2[0].length];
        for (int i = 0; i < matrix1.length; i++) {
            for (int j = 0; j < matrix2[0].length; j++) {
                for (int k = 0; k < matrix1[0].length; k++) {
                    product[i][j] += matrix1[i][k] * matrix2[k][j];
                }
            }
        }
        print(product);
    }

    public static void print(int matrix[][]) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print("[ " + matrix[i][j] + " ] ");
            }
            System.out.println("\n");
        }
    }

    /*This is the my thread class. I have arranged it to take arraylist of Cell Class(it is simple POJO clas that have row and column variables).
     * I am getting location/s that calculated with one thread. Because if I now the which coordinate/s will be calculated with a thread it is easy to implement it at run function.
     * And I am iterating the list with a for loop.*/
    public static class MyThread extends Thread {
        ArrayList<Cell> list;

        public MyThread(ArrayList<Cell> list) {
            this.list = list;
        }

        @Override
        public void run() {
            super.run();
            int result = 0;
            for (int i = 0; i < list.size(); i++) {
                int row = list.get(i).getRow();
                int column = list.get(i).getColumn();
                for (int j = 0; j < matrix1[0].length; j++) {
                    result += matrix1[row][j] * matrix2[j][column];
                }
                resultMatrix[row][column] = result;
                result = 0;
            }
        }

    }

    public static class Cell {
        int row;
        int column;

        public Cell(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getColumn() {
            return column;
        }

        public void setColumn(int column) {
            this.column = column;
        }
    }
}
