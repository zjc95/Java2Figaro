package trans.patchsim;

import org.eclipse.jdt.core.dom.ASTNode;
import trans.dynamicUtils.DynamicAssign;
import trans.dynamicUtils.DynamicCtrlExpr;
import trans.dynamicUtils.DynamicMsg;

import java.util.ArrayList;

public class TraceList {
    private ArrayList<String> _traceList = new ArrayList<>();
    private boolean _isPass;
    public TraceList(ArrayList<DynamicMsg> messageList, boolean isPass) {
        _isPass = isPass;
        for (DynamicMsg message : messageList)
            if ((message instanceof DynamicAssign) || (message instanceof DynamicCtrlExpr)) {
                ASTNode node = message.getNode();
                if (node != null) _traceList.add(node.toString());
            }
    }

    TraceList(boolean isPass, ArrayList<String> stringList) {
        _isPass = isPass;
        _traceList.addAll(stringList);
    }

    void print() {
        System.out.println(_isPass);
        for (String string : _traceList)
            System.out.println(string);
    }

    static boolean checkEqual(TraceList traceList1, int index1, TraceList traceList2, int index2) {
        String str1 = traceList1._traceList.get(index1);
        String str2 = traceList2._traceList.get(index2);
        return str1.equals(str2);
    }

    String get(int index) {
        return _traceList.get(index);
    }

    int size() { return _traceList.size(); }

    boolean checkPass() { return _isPass; }
}
