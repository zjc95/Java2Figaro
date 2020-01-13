import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Semantic--------------
    val Var_posX = Flip(0.5)
    val Var_posY = Flip(0.5)
    val Var_tempA = RichCPD(Var_posX, 
      (OneOf(true)) -> Flip(0.95),
      (*) -> Flip(0.05))
    val Var_tempB = RichCPD(Var_posX, Var_tempA, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Var_this_even = RichCPD(Var_tempA, 
      (OneOf(true)) -> Flip(0.95),
      (*) -> Flip(0.05))
    val Var_this_size = RichCPD(Var_tempB, 
      (OneOf(true)) -> Flip(0.95),
      (*) -> Flip(0.05))
    val Var_this_ret = RichCPD(Var_tempB, Var_tempA, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Ret = RichCPD(Var_this_ret, 
      (OneOf(true)) -> Flip(0.95),
      (*) -> Flip(0.05))

    //-------------Observation--------------
    Var_posX.observe(true)
    Var_posY.observe(true)

    //-------------Constraint--------------
    Var_this_even.addConstraint((b: Boolean) => if (b) 0.8 else 0.19999999999999996)
    Var_this_size.addConstraint((b: Boolean) => if (b) 0.9 else 0.09999999999999998)

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println("Probability of test:" + samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}
