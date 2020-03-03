package introclassJava;
import trans.trace.Dumper;
class IntObj {
  public int value;
  public IntObj(){
  }
  public IntObj(  int i){
    value=i;
  }
}
class FloatObj {
  public float value;
  public FloatObj(){
  }
  public FloatObj(  float i){
    value=i;
  }
}
class LongObj {
  public long value;
  public LongObj(){
  }
  public LongObj(  long i){
    value=i;
  }
}
class DoubleObj {
  public double value;
  public DoubleObj(){
  }
  public DoubleObj(  double i){
    value=i;
  }
}
class CharObj {
  public char value;
  public CharObj(){
  }
  public CharObj(  char i){
    value=i;
  }
}
public class checksum_2c155667_003 {
  public java.util.Scanner scanner;
  public String output="";
  public static void main(  String[] args) throws Exception {
    checksum_2c155667_003 mainClass=new checksum_2c155667_003();
    String output;
    if (args.length > 0) {
      mainClass.scanner=new java.util.Scanner(args[0]);
    }
 else {
      mainClass.scanner=new java.util.Scanner(System.in);
    }
    mainClass.exec();
    System.out.println(mainClass.output);
  }
  public void exec() throws Exception {
    CharObj character=(CharObj)trans.trace.Dumper.dump(new CharObj(),1,60,16);
    CharObj remainder=(CharObj)trans.trace.Dumper.dump(new CharObj(),1,61,16);
    IntObj sum=(IntObj)trans.trace.Dumper.dump(new IntObj(),1,62,15);
    output+=(String)trans.trace.Dumper.dump((String.format("Enter an abitrarily long string, ending with carriage return > ")),1,63,8);
    sum.value=trans.trace.Dumper.dump(0,1,66,8);
    while (trans.trace.Dumper.dump(character.value != '\n',5,67,15)) {
      trans.trace.Dumper.dump(2,67,8);
      try {
        trans.trace.Dumper.dump(2,68,12);
        character.value=trans.trace.Dumper.dump(scanner.findInLine(".").charAt(0),1,69,16);
      }
 catch (      java.lang.NullPointerException e) {
        trans.trace.Dumper.dump(2,68,12);
        trans.trace.Dumper.dump(2,70,14);
        trans.trace.Dumper.dump(e,3,70,21);
        character.value=trans.trace.Dumper.dump('\n',1,71,16);
      }
      ;
      sum.value=trans.trace.Dumper.dump(sum.value + (int)character.value,1,73,12);
    }
    remainder.value=trans.trace.Dumper.dump((char)((sum.value % 64) + 22),1,75,8);
    output+=(String)trans.trace.Dumper.dump((String.format("Check sum is %c\n",remainder.value)),1,76,8);
    if (true) {
      trans.trace.Dumper.dump(2,77,8);
      trans.trace.Dumper.write();
      return;
    }
    ;
    trans.trace.Dumper.write();
  }
}
