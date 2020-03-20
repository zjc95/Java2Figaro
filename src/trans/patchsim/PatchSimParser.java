package trans.patchsim;

import trans.common.LevelLogger;
import trans.common.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

public class PatchSimParser {

    public static double diffTrace(TraceList originTraceList, TraceList patchTraceList) {
        int originSize = originTraceList.size();
        int patchSize = patchTraceList.size();

        int[][] f = new int[2][patchSize + 1];
        for (int i = 1; i <= originSize; i++) {
            for (int j = 1; j <= patchSize; j++) {
                if (TraceList.checkEqual(originTraceList, i - 1, patchTraceList, j - 1)) {
                    f[i % 2][j] = f[(i - 1) % 2][j - 1] + 1;
                } else f[i % 2][j] = Math.max(f[(i - 1) % 2][j], f[i % 2][j - 1]);
            }
        }
        return f[originSize%2][patchSize];
    }

    private static void print(ArrayList<TraceList> originTraceLists, File outputFile) {
        for (TraceList traceList : originTraceLists) {
            for (int i = 0; i < traceList.size(); i++)
                Util.write(traceList.get(i), outputFile, true);
            Util.write(traceList.checkPass() ? "true" : "false", outputFile, true);
        }
    }

    private static ArrayList<TraceList> loadTraceList(File file) {
        ArrayList<TraceList> traceLists = new ArrayList<>();
        try {
            InputStream in = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(in, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            ArrayList<String> stringList = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals("true")) {
                    traceLists.add(new TraceList(true, stringList));
                    stringList.clear();
                }
                else if (line.equals("false")) {
                    traceLists.add(new TraceList(false, stringList));
                    stringList.clear();
                }
                else stringList.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return traceLists;
    }

    public static boolean analyzeByFiles(File workDirectory) {
        ArrayList<TraceList> originTraceLists = loadTraceList(new File(workDirectory, "originTrace.txt"));
        ArrayList<TraceList> patchTraceLists = loadTraceList(new File(workDirectory, "patchTrace.txt"));
        /*System.out.println("Origin");
        for (TraceList traceList : originTraceLists) {
            traceList.print();
            System.out.println("------------------");
        }
        System.out.println("Patch");
        for (TraceList traceList : patchTraceLists) {
            traceList.print();
            System.out.println("------------------");
        }*/
        return analyze(originTraceLists, patchTraceLists);
    }

    public static boolean analyze(ArrayList<TraceList> originTraceLists, ArrayList<TraceList> patchTraceLists) {
        int size = originTraceLists.size();
        if (patchTraceLists.size() != size) {
            LevelLogger.error("ERROR : Wrong Size Of TraceLists " + size + " " + patchTraceLists.size());
            return true;
        }

        //String outPath = "D:\\program\\workspace\\Java2FigaroData";
        //print(originTraceLists, outPath + "\\originTrace.txt");
        //print(patchTraceLists, outPath + "\\patchTrace.txt");

        double[] distanceArray = new double[size];
        for (int i = 0; i < size; i++)
        {
            TraceList originTraceList = originTraceLists.get(i);
            TraceList patchTraceList = patchTraceLists.get(i);
            double length = Math.max(originTraceList.size(), patchTraceList.size());
            double LCS = diffTrace(originTraceList, patchTraceList);
            if (length == 0) distanceArray[i] = 1;
            else distanceArray[i]= 1 - LCS / length;
        }
        LevelLogger.debug("Distance : " + Arrays.toString(distanceArray));

        double maxDistancePass = 0;
        double sumDistanceFail = 0;
        double numFail = 0;
        for (int i = 0; i < size; i++) {
            if (originTraceLists.get(i).checkPass())
                maxDistancePass = Math.max(maxDistancePass, distanceArray[i]);
            else {
                sumDistanceFail += distanceArray[i];
                numFail++;
            }
        }

        //System.out.printf("%.4f %.4f\n",maxDistancePass, sumDistanceFail / numFail);
        return (maxDistancePass < 0.25) && (maxDistancePass <= sumDistanceFail / numFail);
    }

    public static boolean analyzeRedundant(ArrayList<TraceList> originTraceLists, ArrayList<TraceList> patchTraceLists) {
        int size = originTraceLists.size();
        if (patchTraceLists.size() != size) {
            LevelLogger.error("ERROR : Wrong Size Of TraceLists " + size + " " + patchTraceLists.size());
            return false;
        }

        double[][] distanceMatrix = new double[size][size];
        double[] lengthArray =new double[size];
        double[] LCSArray =new double[size];
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size ; j++)
            {
                TraceList originTraceList = originTraceLists.get(i);
                TraceList patchTraceList = patchTraceLists.get(j);
                double length = Math.max(originTraceList.size(), patchTraceList.size());
                double LCS = diffTrace(originTraceList, patchTraceList);
                if (i == j) {
                    lengthArray[i] = length;
                    LCSArray[i] = LCS;
                }
                if (length == 0) distanceMatrix[i][j] = 1;
                else distanceMatrix[i][j]= 1 - LCS / length;
            }

        double maxDistancePass = 0;
        double sumDistancePass = 0;
        double sumDistanceFail = 0;
        double numPass = 0;
        double numFail = 0;
        for (int i = 0; i < size; i++) {
            TraceList originTraceList = originTraceLists.get(i);
            if (originTraceList.checkPass()) {
                maxDistancePass = Math.max(maxDistancePass, distanceMatrix[i][i]);
                sumDistancePass += distanceMatrix[i][i];
                numPass++;
            }
            else {
                sumDistanceFail += distanceMatrix[i][i];
                numFail++;
            }
        }
        double meanDistancePass = sumDistancePass / numPass;
        double meanDistanceFail = sumDistanceFail / numFail;
        System.out.printf("%.4f %.4f\n",meanDistancePass,meanDistanceFail);
        return (maxDistancePass >= 0.25) || (maxDistancePass > sumDistanceFail / numFail);
    }
}
