package trans.trace;

import org.json.simple.*;
import org.json.simple.JSONArray;
import trans.demo.LevelLogger;

import java.io.*;

public class Dumper {
    private static String OUT_FILE_NAME = System.getProperty("user.dir") + "\\resources\\DumpResult.json";
    private static String _fileName = null;
    private static JSONArray _result = null;


    @SuppressWarnings("unchecked")
    public static Object dump(Object object, int type, int line, int column) {
        JSONObject json = new JSONObject();
        json.put("line", line);
        json.put("column", column);
        json.put("type", getTypeString(type));
        json.put("value", getValue(object));
        _result.add(json);
        return object;
    }

    @SuppressWarnings("unchecked")
    public static void dump(int type, int line, int column) {
        JSONObject json = new JSONObject();
        json.put("line", line);
        json.put("column", column);
        json.put("type", getTypeString(type));
        _result.add(json);
    }

    public static void init(String fileName) {
        _fileName = fileName;
        _result = new JSONArray();
    }

    public static boolean write() {
        File file = new File(OUT_FILE_NAME);
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "UTF-8"));
            bufferedWriter.write(genOutString());
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException e) {
            return false;
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static String genOutString() {
        JSONObject object = new JSONObject();
        object.put("file", _fileName);
        object.put("data", _result);
        return object.toString();
    }

    private static String getValue(Object value) {
        if (value.getClass().isPrimitive() || value.getClass() == java.lang.Short.class
                || value.getClass() == java.lang.Long.class || value.getClass() == java.lang.String.class
                || value.getClass() == java.lang.Integer.class || value.getClass() == java.lang.Float.class
                || value.getClass() == java.lang.Byte.class || value.getClass() == java.lang.Character.class
                || value.getClass() == java.lang.Double.class || value.getClass() == java.lang.Boolean.class
                || value.getClass() == java.util.Date.class || value.getClass().isEnum()) {
            return "\"" + value + "\"";
        }
        return "";
    }

    private static String getTypeString(int type) {
        switch (type) {
            case TraceUtil.TRACE_TYPE_ASSIGN: return "ASSIGN";
            case TraceUtil.TRACE_TYPE_STMT: return "STMT";
            case TraceUtil.TRACE_TYPE_ENTRY: return "ENTRY";
            case TraceUtil.TRACE_TYPE_RET: return "RET";
            case TraceUtil.TRACE_TYPE_CONTROL: return "CONTROL";
            default:
                LevelLogger.error("UNKNOWN TYPE" + type);
                return "UNKNOWN";
        }
    }

    public static int dump(int o, int type, int line, int column) {
        return (Integer) dump(Integer.valueOf(o), type, line, column);
    }

    public static float dump(float o, int type, int line, int column) {
        return (Float) dump(Float.valueOf(o), type, line, column);
    }

    public static double dump(double o, int type, int line, int column) {
        return (Double) dump(Double.valueOf(o), type, line, column);
    }

    public static long dump(long o, int type, int line, int column) {
        return (Long) dump(Long.valueOf(o), type, line, column);
    }

    public static byte dump(byte o, int type, int line, int column) {
        return (Byte) dump(Byte.valueOf(o), type, line, column);
    }

    public static char dump(char o, int type, int line, int column) {
        return (Character) dump(Character.valueOf(o), type, line, column);
    }

    public static boolean dump(boolean o, int type, int line, int column) {
        return (Boolean) dump(Boolean.valueOf(o), type, line, column);
    }
}
