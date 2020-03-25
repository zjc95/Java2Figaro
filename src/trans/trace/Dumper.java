package trans.trace;

import org.json.simple.*;
import org.json.simple.JSONArray;
import trans.common.LevelLogger;
import trans.common.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Dumper {
    private static JSONArray _result  = new JSONArray();

    @SuppressWarnings("unchecked")
    public static Object dump(Object object, int type, int line, int column) {
        JSONObject json = new JSONObject();
        json.put("line", line);
        json.put("column", column);
        json.put("type", getTypeString(type));
        json.put("value", getValue(object));
        _result.add(json);
        if (type == TraceUtil.TRACE_TYPE_RET)
            write();
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

    public static void write() {
        File file = new File(System.getProperty("user.dir"), "DumpResult.json");
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8));
            bufferedWriter.write(genOutString());
            bufferedWriter.write("\n");
            bufferedWriter.close();
        } catch (IOException ignored) {
        } finally {
            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static String genOutString() {
        JSONObject object = new JSONObject();
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
            return value.toString();
        }
        return "";
    }

    private static String getTypeString(int type) {
        switch (type) {
            case TraceUtil.TRACE_TYPE_ASSIGN: return "ASSIGN";
            case TraceUtil.TRACE_TYPE_STMT_BEGIN: return "STMT";
            case TraceUtil.TRACE_TYPE_STMT_END: return "END";
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
