import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Semantic--------------
    val Var_scanner = Flip(0.5)
    val Var_output = Flip(0.5)
    val Var_v1 = Flip( 0.7)
    val Var_v2 = Flip( 0.7)
    val Var_v3 = Flip( 0.7)
    val Var_v4 = Flip( 0.7)
    val Var_output_1 = If(Var_output, Flip(0.99), Flip(0.05))
    val Var_v1_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Var_v2_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Var_v3_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Var_v4_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Control_69_12 = RichCPD(Var_v2_value, Var_v1_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_69_8 = If(Control_69_12, Flip(0.99), Flip(0.05))
    val Control_71_19 = RichCPD(Var_v2_value, Var_v1_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_71_15 = RichCPD(Control_71_19, Stmt_69_8, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Control_74_20 = RichCPD(Var_v3_value, Var_v4_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_74_15 = RichCPD(Control_74_20, Stmt_71_15, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Var_output_2 = RichCPD(Var_output_1, Var_v3_value, Stmt_74_15, 
      (OneOf(true), OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *, *) -> Flip(0.05))
    val Stmt_79_8 = Flip( 0.7)
    val Ret = If(Var_output_2, Flip(0.99), Flip(0.05))

    //-------------Observation--------------
    Var_scanner.observe(true)
    Var_output.observe(true)

    //-------------Constraint--------------
    Control_69_12.addConstraint((b: Boolean) => if (b) 0.4 else 0.6)
    Control_71_19.addConstraint((b: Boolean) => if (b) 0.4 else 0.6)

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println(samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}
