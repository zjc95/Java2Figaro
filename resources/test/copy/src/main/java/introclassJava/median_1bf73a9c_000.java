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
public class median_1bf73a9c_000 {
  public java.util.Scanner scanner;
  public String output="";
  public static void main(  String[] args) throws Exception {
    median_1bf73a9c_000 mainClass=new median_1bf73a9c_000();
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
    IntObj first=(IntObj)trans.trace.Dumper.dump(new IntObj(),1,60,15), second=(IntObj)trans.trace.Dumper.dump(new IntObj(),1,60,38), third=(IntObj)trans.trace.Dumper.dump(new IntObj(),1,60,62), ans=(IntObj)trans.trace.Dumper.dump(new IntObj(),1,61,27);
    output+=(String)trans.trace.Dumper.dump((String.format("Please enter 3 numbers sepereted by spaces > ")),1,62,8);
    first.value=trans.trace.Dumper.dump(scanner.nextInt(),1,64,8);
    second.value=trans.trace.Dumper.dump(scanner.nextInt(),1,65,8);
    third.value=trans.trace.Dumper.dump(scanner.nextInt(),1,66,8);
    if ((trans.trace.Dumper.dump(first.value > second.value,5,67,13) && trans.trace.Dumper.dump(first.value < third.value,5,67,43)) || (trans.trace.Dumper.dump(first.value > third.value,5,68,20) && trans.trace.Dumper.dump(first.value < second.value,5,68,49))) {
      trans.trace.Dumper.dump(2,67,8);
      ans.value=trans.trace.Dumper.dump(first.value,1,69,12);
    }
 else {
      trans.trace.Dumper.dump(2,67,8);
      if ((trans.trace.Dumper.dump(second.value > first.value,5,70,20) && trans.trace.Dumper.dump(second.value < third.value,5,70,50)) || (trans.trace.Dumper.dump(second.value > third.value,5,71,23) && trans.trace.Dumper.dump(second.value < first.value,5,71,53))) {
        trans.trace.Dumper.dump(2,70,15);
        ans.value=trans.trace.Dumper.dump(second.value,1,72,12);
      }
 else {
        trans.trace.Dumper.dump(2,70,15);
        ans.value=trans.trace.Dumper.dump(third.value,1,74,12);
      }
    }
    output+=(String)trans.trace.Dumper.dump((String.format("%d is the median\n",ans.value)),1,76,8);
    if (true) {
      trans.trace.Dumper.dump(2,77,8);
      trans.trace.Dumper.write();
      return;
    }
    ;
    trans.trace.Dumper.write();
  }
}
