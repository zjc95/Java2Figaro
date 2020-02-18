import trans.trace.Dumper;
class test {
  public void func(  Tmp tmp){
    trans.trace.Dumper.dump(tmp,3,3,18);
    Tmp tmpA=(Tmp)trans.trace.Dumper.dump(tmp,1,4,6);
    Tmp tmpB=(Tmp)trans.trace.Dumper.dump(tmp,1,5,6);
    int tempA=trans.trace.Dumper.dump(tmpA.even - 2,1,6,6);
    int tempB=trans.trace.Dumper.dump(tempA - tmpB.even,1,7,6);
    tmpA.even=trans.trace.Dumper.dump(tempA,1,8,2);
    tmpA.size=trans.trace.Dumper.dump(tempB,1,9,2);
    tmpB.even=trans.trace.Dumper.dump(tempA,1,10,2);
    tmpB=(Tmp)trans.trace.Dumper.dump(tmpA,1,11,2);
    tmpB.size=trans.trace.Dumper.dump(tempB,1,12,2);
    int ret=trans.trace.Dumper.dump(tmpB.size + tmpB.even + 2,1,13,6);
    return trans.trace.Dumper.dump(ret,4,14,2);
  }
class Tmp {
    public int even;
    public int size;
  }
}
