import java.io.File;
import trans.trace.Dumper;
class testParser {
  static int even;
  static int size;
  static int ret;
  public static int func(  int posX,  int posY){
    trans.trace.Dumper.dump(posX,3,8,24);
    trans.trace.Dumper.dump(posY,3,8,34);
    int tempA=trans.trace.Dumper.dump(posX - 2,1,9,6);
    int tempB=trans.trace.Dumper.dump(tempA - posX,1,10,6);
    even=trans.trace.Dumper.dump(tempA,1,11,2);
    size=trans.trace.Dumper.dump(tempB,1,12,2);
    ret=trans.trace.Dumper.dump(tempA + tempB + 2,1,13,2);
    return trans.trace.Dumper.dump(ret,4,14,2);
  }
  public static void main(  String args[]){
    System.out.println(func(1,4));
  }
}
