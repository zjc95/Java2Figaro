import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Semantic--------------
    val Var_scanner = Flip(0.5)
    val Var_output = Flip(0.5)
    val Var_first = Flip( 0.7)
    val Var_second = Flip( 0.7)
    val Var_third = Flip( 0.7)
    val Var_ans = Flip( 0.7)
    val Var_output_1 = If(Var_output, Flip(0.99), Flip(0.05))
    val Var_first_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Var_second_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Var_third_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Control_67_13 = RichCPD(Var_second_value, Var_first_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Control_67_44 = RichCPD(Var_third_value, Var_first_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_67_8 = RichCPD(Control_67_13, Control_67_44, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Var_ans_value = RichCPD(Var_first_value, Stmt_67_8, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Var_ans_value_1 = If(Var_third_value, Flip(0.99), Flip(0.05))
    val Var_output_2 = RichCPD(Var_output_1, Var_ans_value_1, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_78_8 = Flip( 0.7)
    val Ret = If(Var_output_2, Flip(0.99), Flip(0.05))

    //-------------Observation--------------
    Var_scanner.observe(true)
    Var_output.observe(true)

    //-------------Constraint--------------
    Ret.addConstraint((b: Boolean) => if (b) 0.3 else 0.7)
    Var_ans_value_1.addConstraint((b: Boolean) => if (b) 0.3 else 0.7)

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println(samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}
