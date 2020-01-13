import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Semantic--------------
    val Var_patch = Flip(0.5)
    val Var_posX = Flip(0.5)
    val Var_posY = Flip(0.5)
    val Control_9_7 = If(Var_patch, Flip(0.95), Flip(0.05))
    val Stmt_9_2 = If(Control_9_7, Flip(0.95), Flip(0.05))
    val Control_11_11 = If(Var_patch, Flip(0.95), Flip(0.05))
    val Stmt_11_7 = RichCPD(Control_11_11, Stmt_9_2, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Var_tempA = RichCPD(Var_posX, Stmt_11_7, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Var_tempB = RichCPD(Var_posX, Var_tempA, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Var_this_even = If(Var_tempA, Flip(0.95), Flip(0.05))
    val Var_this_size = If(Var_tempB, Flip(0.95), Flip(0.05))
    val Var_this_ret = RichCPD(Var_tempB, Var_tempA, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Ret = If(Var_this_ret, Flip(0.95), Flip(0.05))

    //-------------Observation--------------
    Var_patch.observe(true)
    Var_posX.observe(true)
    Var_posY.observe(true)

    //-------------Constraint--------------
    Var_this_even.addConstraint((b: Boolean) => if (b) 0.2 else 0.8)
    Var_this_size.addConstraint((b: Boolean) => if (b) 0.9 else 0.09999999999999998)

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println("Probability of test:" + samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}
