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
public class grade_1b31fa5c_003 {
  public java.util.Scanner scanner;
  public String output="";
  public static void main(  String[] args) throws Exception {
    grade_1b31fa5c_003 mainClass=new grade_1b31fa5c_003();
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
    FloatObj A=(FloatObj)trans.trace.Dumper.dump(new FloatObj(),1,60,17), B=(FloatObj)trans.trace.Dumper.dump(new FloatObj(),1,60,38), C=(FloatObj)trans.trace.Dumper.dump(new FloatObj(),1,60,59), D=(FloatObj)trans.trace.Dumper.dump(new FloatObj(),1,61,29), grade=(FloatObj)trans.trace.Dumper.dump(new FloatObj(),1,61,50);
    output+=(String)trans.trace.Dumper.dump((String.format("Enter thresholds for A, B, C, D\n")),1,62,8);
    output+=(String)trans.trace.Dumper.dump((String.format("in that order, decreasing percentages > ")),1,63,8);
    A.value=trans.trace.Dumper.dump(scanner.nextFloat(),1,64,8);
    B.value=trans.trace.Dumper.dump(scanner.nextFloat(),1,65,8);
    C.value=trans.trace.Dumper.dump(scanner.nextFloat(),1,66,8);
    D.value=trans.trace.Dumper.dump(scanner.nextFloat(),1,67,8);
    output+=(String)trans.trace.Dumper.dump((String.format("Thank you. Now enter student score (percent) >")),1,68,8);
    grade.value=trans.trace.Dumper.dump(scanner.nextFloat(),1,70,8);
    if (trans.trace.Dumper.dump(grade.value >= A.value,5,71,12)) {
      trans.trace.Dumper.dump(2,71,8);
      output+=(String)trans.trace.Dumper.dump((String.format("Student has an A grade\n")),1,72,12);
      if (true) {
        trans.trace.Dumper.dump(2,73,12);
        trans.trace.Dumper.write();
        return;
      }
      ;
    }
 else {
      trans.trace.Dumper.dump(2,71,8);
      if ((trans.trace.Dumper.dump(grade.value < A.value,5,75,20)) && (trans.trace.Dumper.dump(grade.value >= B.value,5,75,47))) {
        trans.trace.Dumper.dump(2,75,15);
        output+=(String)trans.trace.Dumper.dump((String.format("Student has an B grade\n")),1,76,12);
        if (true) {
          trans.trace.Dumper.dump(2,77,12);
          trans.trace.Dumper.write();
          return;
        }
        ;
      }
 else {
        trans.trace.Dumper.dump(2,75,15);
        if ((trans.trace.Dumper.dump(grade.value < B.value,5,79,20)) && (trans.trace.Dumper.dump(grade.value >= A.value,5,79,47))) {
          trans.trace.Dumper.dump(2,79,15);
          output+=(String)trans.trace.Dumper.dump((String.format("Student has an C grade\n")),1,80,12);
          if (true) {
            trans.trace.Dumper.dump(2,81,12);
            trans.trace.Dumper.write();
            return;
          }
          ;
        }
 else {
          trans.trace.Dumper.dump(2,79,15);
          if ((trans.trace.Dumper.dump(grade.value < C.value,5,83,20)) && (trans.trace.Dumper.dump(grade.value >= D.value,5,83,47))) {
            trans.trace.Dumper.dump(2,83,15);
            output+=(String)trans.trace.Dumper.dump((String.format("Student has an D grade\n")),1,84,12);
            if (true) {
              trans.trace.Dumper.dump(2,85,12);
              trans.trace.Dumper.write();
              return;
            }
            ;
          }
 else {
            trans.trace.Dumper.dump(2,83,15);
            output+=(String)trans.trace.Dumper.dump((String.format("Student has failed the course\n")),1,88,12);
          }
        }
      }
    }
    if (true) {
      trans.trace.Dumper.dump(2,90,8);
      trans.trace.Dumper.write();
      return;
    }
    ;
    trans.trace.Dumper.write();
  }
}
