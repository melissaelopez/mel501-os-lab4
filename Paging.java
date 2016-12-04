/* 
Paging.java
Melissa Lopez
N16365076
mel501@nyu.edu
*/

/*
J=1: One process with A=1 and B=C=0, the simplest (fully sequential) case.
J=2: Four processes, each with A=1 and B=C=0.
J=3: Four processes, each with A=B=C=0 (fully random references).
J=4: One process with A=.75, B=.25 and C=0; one process with A=.75, B=0, and C=.25; one process with A=.75, B=.125 and C=.125; and one process with A=.5, B=.125 and C=.125.
*/

import java.io.*;
import java.util.ArrayList;

public class Paging {

    public static void main(String[] args) {

        int machineSize = Integer.parseInt(args[0]);
        int pageSize = Integer.parseInt(args[1]);
        int processSize = Integer.parseInt(args[2]);
        int jobMix = Integer.parseInt(args[3]);
        int numRef = Integer.parseInt(args[4]);
        String algo = args[5];
        int debug = Integer.parseInt(args[6]);

        System.out.println("\n********************************************");
        System.out.println("The machine size is " + machineSize + ".");  // in words
        System.out.println("The page size is " + pageSize + ".");     // in words
        System.out.println("The process size is " + processSize + ".");  // i.e., the references are to virtual addresses 0..S-1.
        System.out.println("The job mix number is " + jobMix + ".");
        System.out.println("The number of references per process is " + numRef + ".");
        System.out.println("The replacement algorithm is " + algo + ".");
        System.out.println("The level of debugging output is " + debug + ".");
        System.out.println("********************************************\n");

        int q = 3;

        Process[] processArr = startProcesses(machineSize, pageSize, processSize, jobMix, numRef, debug);
    }

    /* 
    Find the starting w for each process, and store in a processArr
    */
    public static Process[] startProcesses(int machineSize, int pageSize, int processSize, int jobMix, int numRef, int debug){
        Process[] processArr;
        if (jobMix == 1){
            int w = (111 * 1 + processSize) % processSize;
            processArr = new Process[1];
            processArr[0] = new Process(w);
            for (int i = 0; i < numRef; i++){
                System.out.println(w);
                w = (w + 1 + processSize) % processSize;
            } 
        } else {
            int k = 1;
            processArr = new Process[1];
            for (int i = 0; i < 4; i ++){
                int w = (111 * 1 + processSize) % processSize;
                processArr[i] = new Process(w);
                for (int j = 0; j < numRef; j++){
                    System.out.println(w);
                    w = (w + 1 + processSize) % processSize;
                }
                k++;
            }
        }
        return processArr;    
    }

    // public static int[][] createFrameTable(int machineSize, int pageSize, int processSize, int jobMix, int numRef, int debug){
    //     tableEntry[][] frameTable

    //     return frameTable;
    // }
}

class Process{
    public int w;
    public int faults = 0;
    public int evictions = 0;
    public int residencyTime = 0; // time that the page was evicted minus the time it was loaded.

    public Process(int w){
        this.w = w;
    }

}
