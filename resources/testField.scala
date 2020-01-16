import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Semantic--------------
    val Var_tmp = Flip(0.5)
    val Var_tmpA = If(Var_tmp, Flip(0.95), Flip(0.05))
    val Var_tmpB = If(Var_tmp, Flip(0.95), Flip(0.05))
    val Var_tmpA_even = If(Var_tmpA, Flip(0.95), Flip(0.05))
    val Var_tempA = If(Var_tmpA_even, Flip(0.95), Flip(0.05))
    val Var_tmpB_even = If(Var_tmpB, Flip(0.95), Flip(0.05))
    val Var_tempB = RichCPD(Var_tmpB_even, Var_tempA, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Var_tmpA_even_1 = If(Var_tempA, Flip(0.95), Flip(0.05))
    val Var_tmpA_size = If(Var_tempB, Flip(0.95), Flip(0.05))
    val Var_tmpB_even_1 = If(Var_tempA, Flip(0.95), Flip(0.05))
    val Var_tmpA_1 = RichCPD(Var_tmpA_even_1, Var_tmpA_size, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Var_tmpB_1 = If(Var_tmpA_1, Flip(0.95), Flip(0.05))
    val Var_tmpB_size = If(Var_tempB, Flip(0.95), Flip(0.05))
    val Var_tmpB_even_2 = If(Var_tmpB_1, Flip(0.95), Flip(0.05))
    val Var_ret = RichCPD(Var_tmpB_even_2, Var_tmpB_size, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Ret = If(Var_ret, Flip(0.95), Flip(0.05))

    //-------------Observation--------------
    Var_tmp.observe(true)

    //-------------Constraint--------------
    Var_tmpA_even_1.addConstraint((b: Boolean) => if (b) 0.2 else 0.8)
    Var_tmpB_even_1.addConstraint((b: Boolean) => if (b) 0.2 else 0.8)
    Var_tmpA_size.addConstraint((b: Boolean) => if (b) 0.1 else 0.9)
    Var_tmpB_size.addConstraint((b: Boolean) => if (b) 0.1 else 0.9)

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println("Probability of test:" + samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}
