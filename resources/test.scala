import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Semantic--------------
    val Var_posX = Flip(0.5)
    val Var_posY = Flip(0.5)
    val Var_tempA = If(Var_posX, Flip(0.95), Flip(0.05))
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
    Var_posX.observe(true)
    Var_posY.observe(true)

    //-------------Constraint--------------
    Var_this_even.addConstraint((b: Boolean) => if (b) 0.2 else 0.8)
    Var_this_size.addConstraint((b: Boolean) => if (b) 0.1 else 0.9)

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println("Probability of test:" + samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}
