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

        pager(machineSize, pageSize, processSize, jobMix, numRef, debug, algo);
    }

    public static void pager(int machineSize, int pageSize, int processSize, int jobMix, int numRef, int debug, String algo){

        // Setup stuff

        Process[] processArr = startProcesses(machineSize, pageSize, processSize, jobMix, numRef, debug); printProcesses(processArr);
        Entry[][] frameTable = createFrameTable(machineSize, pageSize, processSize, jobMix, numRef, debug);

        // Determine proper algorithm

        if (algo.equals("lru")){
            lru(machineSize, pageSize, processSize, jobMix, numRef, debug, processArr, frameTable);
        }

        displayOutput(processArr);
    }

    public static void lru(int machineSize, int pageSize, int processSize, int jobMix, int numRef, int debug, Process[] processArr, Entry[][] frameTable){
        System.out.printf("LRU starting! \n");
        int numProcesses = 4;
        if (jobMix == 1){
            numProcesses = 1;
        } 

        int time = 1;
        for (int k = 0; k < (int) Math.ceil((double) numRef * numProcesses / 3); k++){
            // each process
            for (int i = 0; i < numProcesses; i++){
                // should go three times
                for (int j = 0; j < 3; j++){
                    // unless it has already gone enough times
                    if (processArr[i].count >= numRef - 1){
                        break;
                    } else {
                        processArr[i].count++;
                        // find the requested page number

                        int requestedPage = (processArr[i].refs[processArr[i].count] / pageSize);
                        String message = checkFrameTable(frameTable, processArr[i], requestedPage, time, "lru", processArr);
                        System.out.printf("Process %d references word %d (page %d) at time %d: %s \n", i+1, processArr[i].refs[processArr[i].count], requestedPage, time, message);
                        printFrameTable(frameTable);
                        time++;
                    }
                    
                }
            }
        }
    }

    /* 
    Find the starting w for each process, and store in a processArr
    */
    public static Process[] startProcesses(int machineSize, int pageSize, int processSize, int jobMix, int numRef, int debug){
        Process[] processArr;
        if (jobMix == 1){
            int w = (111 * 1 + processSize) % processSize;
            processArr = new Process[1];
            int[] refs = new int[numRef];
            processArr[0] = new Process(w, 1);
            for (int i = 0; i < numRef; i++){
                System.out.println(w);
                refs[i] = w;
                w = (w + 1 + processSize) % processSize;
            }
            processArr[0].refs = refs;
        } else {
            int k = 1;
            processArr = new Process[4];
            for (int i = 0; i < 4; i ++){
                int[] refs = new int[numRef];
                int w = (111 * (i+1) + processSize) % processSize;
                processArr[i] = new Process(w, i+1);
                for (int j = 0; j < numRef; j++){
                    System.out.println(w);
                    refs[j] = w;
                    w = (w + 1 + processSize) % processSize;
                }
                processArr[i].refs = refs;
                k++;
            }
        }
        return processArr;    
    }

    public static Entry[][] createFrameTable(int machineSize, int pageSize, int processSize, int jobMix, int numRef, int debug){
        Entry[][] frameTable = new Entry[machineSize / pageSize][pageSize];
        for (int i = 0; i < machineSize/pageSize; i++){
            for (int j = 0; j < pageSize; j++){
                frameTable[i][j] = new Entry(-1, -1, -1, -1);
            }
        }
        printFrameTable(frameTable);
        return frameTable;
    }

    public static void printFrameTable(Entry[][] frameTable){
        System.out.printf("+----------+----------+----------+----+----+----+----+----+----+----+----+----+----+\n");
        for (int i = 0; i < frameTable.length; i++){
            System.out.printf("| Frame %2d |", i);
            System.out.printf("P#%2d Pg%2d |", frameTable[i][0].processNum, frameTable[i][0].pageNum);
            System.out.printf("  Last: %2d |", frameTable[i][0].lastTimeUsed);
            for (int j = 0; j < frameTable[0].length; j++){
                System.out.printf("%3d |", frameTable[i][j].address);
            }
            System.out.printf("\n+----------+----------+----------+----+----+----+----+----+----+----+----+----+----+");
            System.out.printf("\n");
        }
    }

    public static void printProcesses(Process[] processArr){
        for (int i = 0; i < processArr.length; i++){
            System.out.printf("Process %d:\n", i+1);
            System.out.printf("     Start at: %d\n", processArr[i].w);
            System.out.printf("     Faults: %d\n", processArr[i].faults);
            System.out.printf("     Evictions: %d\n", processArr[i].evictions);
            System.out.printf("     Residency Time: %d\n", processArr[i].residencyTime);
        }
    }

    public static String checkFrameTable(Entry[][] frameTable, Process process, int requestedPage, int time, String algo, Process[] processArr){
        boolean found = false;
        int location = -1;
        for (int i = 0; i < frameTable.length; i++){
            if (frameTable[i][0].processNum == process.processNum && frameTable[i][0].pageNum == requestedPage){
                found = true;
                location = i;
                break;
            }
        }
        if (found){
            frameTable[location][0].lastTimeUsed = time;
            return "Hit in frame " + location;
        } else {
            return "Fault, " + handleFault(frameTable, process, requestedPage, time, algo, processArr);
        }
    }

    public static String handleFault(Entry[][] frameTable, Process process, int requestedPage, int time, String algo, Process[] processArr){
        String message = "";
        process.faults++;

        // if: there are free frames, choose the highest numbered free frame
        int highestFreeFrame = highestFreeFrame(frameTable);
        if (highestFreeFrame != -1){
            int frame = insertPage(frameTable, process, requestedPage, highestFreeFrame, time);
            message = "using free frame " + frame;
        } else{
            if (algo.equals("lru")){
                // evict the one that has been unused for the longest time
                int minTime = Integer.MAX_VALUE;
                int toEvict = -1;
                for (int i = 0; i < frameTable.length; i++){
                    if (frameTable[i][0].lastTimeUsed < minTime){
                        minTime = frameTable[i][0].lastTimeUsed;
                        toEvict = i;
                    }
                }
                message = "evicting page " + frameTable[toEvict][0].pageNum + " of process " + frameTable[toEvict][0].processNum + " from frame " + toEvict;
                processArr[frameTable[toEvict][0].processNum - 1].residencyTime += (time - frameTable[toEvict][0].loadTime);
                processArr[frameTable[toEvict][0].processNum - 1].evictions++;
                insertPage(frameTable, process, requestedPage, toEvict, time);
            }
        }

        // else: use the given algorithm to figure out which page to evict
        return message;
    }

    public static int highestFreeFrame(Entry[][] frameTable){
        int freeFrame = -1;
        for (int i = frameTable.length - 1; i > -1; i--){
            if (frameTable[i][0].processNum == -1 && frameTable[i][0].pageNum == -1){
                freeFrame = i;
                break;
            }
        }
        return freeFrame;
    }

    public static int insertPage(Entry[][] frameTable, Process process, int requestedPage, int highestFreeFrame, int time){
        int pageAddress = frameTable[highestFreeFrame].length * requestedPage;
        for (int i = 0; i < frameTable[highestFreeFrame].length; i++){
            frameTable[highestFreeFrame][i] = new Entry(pageAddress, time, process.processNum, requestedPage);
            pageAddress++;
        }
        frameTable[highestFreeFrame][0].lastTimeUsed = time;
        return highestFreeFrame;
    }

    public static void displayOutput(Process[] processArr){
        System.out.printf("\n");
        for (int i = 0; i < processArr.length; i++){
            if (processArr[i].evictions == 0){
                System.out.printf("Process %d had %d faults\n     With no evictions, the average residence is undefined. \n", i+1, processArr[i].faults);
            } else {
                System.out.printf("Process %d had %d faults and %f average residency. \n", i+1, processArr[i].faults, (float) processArr[i].residencyTime/processArr[i].evictions);
            }
        }
    }
}

class Process {
    public int processNum;
    public int w;
    public int count = -1;
    public int faults = 0;
    public int evictions = 0;
    public int residencyTime = 0; // time that the page was evicted minus the time it was loaded.
    public int[] refs;

    public Process(int w, int processNum){
        this.processNum = processNum;
        this.w = w;
    }

}

class Entry {
    public int address;
    public int loadTime;
    public int processNum;
    public int pageNum;
    public int lastTimeUsed = -1;
    public int evictTime = 0;

    public Entry(int address, int loadTime, int processNum, int pageNum){
        this.address = address;
        this.loadTime = loadTime;
        this.processNum = processNum;
        this.pageNum = pageNum;
    }

}

// System.out.printf("LRU starting! \n");
// int numProcesses = 4;
// if (jobMix == 1 || jobMix == 4){
//     numProcesses = 1;
// } 

// for (int k = 0; k < (int) Math.ceil((double) numRef * numProcesses / 3); k++){
//     // each process
//     for (int i = 0; i < numProcesses; i++){
//         // should go three times
//         for (int j = 0; j < 3; j++){
//             // unless it has already gone enough times
//             if (processArr[i].count >= numRef){
//                 break;
//             } else {
//                 System.out.printf("Process %d \n", i+1);
//                 processArr[i].count++;
//             }
            
//         }
//     }
// }



