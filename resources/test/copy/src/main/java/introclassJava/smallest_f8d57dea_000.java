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
public class smallest_f8d57dea_000 {
  public java.util.Scanner scanner;
  public String output="";
  public static void main(  String[] args) throws Exception {
    smallest_f8d57dea_000 mainClass=new smallest_f8d57dea_000();
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
    IntObj v1=(IntObj)trans.trace.Dumper.dump(new IntObj(),1,61,15), v2=(IntObj)trans.trace.Dumper.dump(new IntObj(),1,61,35), v3=(IntObj)trans.trace.Dumper.dump(new IntObj(),1,61,55), v4=(IntObj)trans.trace.Dumper.dump(new IntObj(),1,61,75);
    output+=(String)trans.trace.Dumper.dump((String.format("Please enter 4 numbers separated by spaces > ")),1,63,8);
    v1.value=trans.trace.Dumper.dump(scanner.nextInt(),1,65,8);
    v2.value=trans.trace.Dumper.dump(scanner.nextInt(),1,66,8);
    v3.value=trans.trace.Dumper.dump(scanner.nextInt(),1,67,8);
    v4.value=trans.trace.Dumper.dump(scanner.nextInt(),1,68,8);
    if (trans.trace.Dumper.dump(v1.value < v2.value,5,69,12) && trans.trace.Dumper.dump(v1.value < v3.value,5,69,35) && trans.trace.Dumper.dump(v1.value < v4.value,5,69,58)) {
      trans.trace.Dumper.dump(2,69,8);
      output+=(String)trans.trace.Dumper.dump((String.format("%d is the smallest\n",v1.value)),1,70,12);
    }
 else {
      trans.trace.Dumper.dump(2,69,8);
      if (trans.trace.Dumper.dump(v2.value < v1.value,5,71,19) && trans.trace.Dumper.dump(v2.value < v3.value,5,71,42) && trans.trace.Dumper.dump(v2.value < v4.value,5,72,22)) {
        trans.trace.Dumper.dump(2,71,15);
        output+=(String)trans.trace.Dumper.dump((String.format("%d is the smallest\n",v2.value)),1,73,12);
      }
 else {
        trans.trace.Dumper.dump(2,71,15);
        if ((trans.trace.Dumper.dump(v3.value <= v4.value,5,74,20))) {
          trans.trace.Dumper.dump(2,74,15);
          output+=(String)trans.trace.Dumper.dump((String.format("%d is the smallest\n",v3.value)),1,75,12);
        }
 else {
          trans.trace.Dumper.dump(2,74,15);
          output+=(String)trans.trace.Dumper.dump((String.format("%d is the smallest\n",v4.value)),1,77,12);
        }
      }
    }
    if (true) {
      trans.trace.Dumper.dump(2,79,8);
      trans.trace.Dumper.write();
      return;
    }
    ;
    trans.trace.Dumper.write();
  }
}
