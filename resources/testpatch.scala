import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Semantic--------------
    val Var_scanner = Flip(0.5)
    val Var_first = Flip(0.5)
    val Var_second = Flip(0.5)
    val Var_third = Flip(0.5)
    val Var_ans = Flip(0.5)
    val Var_output = Flip(0.5)
    val Var_first_value = If(Var_scanner, Flip(0.95), Flip(0.05))
    val Var_second_value = If(Var_scanner, Flip(0.95), Flip(0.05))
    val Var_third_value = If(Var_scanner, Flip(0.95), Flip(0.05))
    val Control_67_13 = RichCPD(Var_second_value, Var_first_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Control_68_20 = RichCPD(Var_third_value, Var_first_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Stmt_67_8 = RichCPD(Control_67_13, Control_68_20, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Control_70_20 = RichCPD(Var_second_value, Var_first_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Control_71_23 = RichCPD(Var_second_value, Var_third_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Stmt_70_15 = RichCPD(Control_70_20, Control_71_23, Stmt_67_8, 
      (OneOf(true), OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *, *) -> Flip(0.05))
    val Var_ans_value = RichCPD(Var_third_value, Stmt_70_15, 
      (OneOf(true), OneOf(true)) -> Flip(0.95),
      (*, *) -> Flip(0.05))
    val Var_output_1 = If(Var_ans_value, Flip(0.95), Flip(0.05))
    val Stmt_77_8 = Flip(0.5)
    val Ret = If(Var_output_1, Flip(0.95), Flip(0.05))

    //-------------Observation--------------
    Var_scanner.observe(true)

    //-------------Constraint--------------

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println("Probability of test:" + samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}
