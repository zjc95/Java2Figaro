import trans.trace.Dumper;
class testIF {
  int even;
  int size;
  int ret;
  public void func(  int patch,  int posX,  int posY){
    trans.trace.Dumper.dump(patch,3,6,18);
    trans.trace.Dumper.dump(posX,3,6,29);
    trans.trace.Dumper.dump(posY,3,6,39);
    int tempA;
    int tempB;
    if (trans.trace.Dumper.dump((trans.trace.Dumper.dump(patch < 2,5,9,7)) && (trans.trace.Dumper.dump(patch >= 1,5,9,22)),5,9,6)) {
      trans.trace.Dumper.dump(2,9,2);
      tempA=trans.trace.Dumper.dump(posY - 2,1,10,3);
    }
 else {
      trans.trace.Dumper.dump(2,9,2);
      if (trans.trace.Dumper.dump(patch == 2,5,11,11)) {
        trans.trace.Dumper.dump(2,11,7);
        tempA=trans.trace.Dumper.dump(posX - 2,1,12,3);
        tempA=trans.trace.Dumper.dump(tempA + 1,1,13,3);
      }
 else {
        trans.trace.Dumper.dump(2,11,7);
        tempA=trans.trace.Dumper.dump(posX + 2,1,16,3);
      }
    }
    tempB=trans.trace.Dumper.dump(tempA - posX,1,18,2);
    this.even=trans.trace.Dumper.dump(tempA,1,19,2);
    this.size=trans.trace.Dumper.dump(tempB,1,20,2);
    this.ret=trans.trace.Dumper.dump(tempA + tempB + 2,1,21,2);
    return trans.trace.Dumper.dump(this.ret,4,22,2);
  }
}
