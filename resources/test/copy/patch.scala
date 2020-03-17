import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Semantic--------------
    val Var_scanner = Flip(0.5)
    val Var_output = Flip(0.5)
    val Var_A = Flip( 0.7)
    val Var_B = Flip( 0.7)
    val Var_C = Flip( 0.7)
    val Var_D = Flip( 0.7)
    val Var_grade = Flip( 0.7)
    val Var_output_1 = If(Var_output, Flip(0.99), Flip(0.05))
    val Var_output_2 = If(Var_output_1, Flip(0.99), Flip(0.05))
    val Var_A_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Var_B_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Var_C_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Var_D_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Var_output_3 = If(Var_output_2, Flip(0.99), Flip(0.05))
    val Var_grade_value = If(Var_scanner, Flip(0.99), Flip(0.05))
    val Control_71_12 = RichCPD(Var_A_value, Var_grade_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_71_8 = If(Control_71_12, Flip(0.99), Flip(0.05))
    val Control_75_20 = RichCPD(Var_A_value, Var_grade_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Control_75_47 = RichCPD(Var_B_value, Var_grade_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_75_15 = RichCPD(Control_75_20, Control_75_47, Stmt_71_8, 
      (OneOf(true), OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *, *) -> Flip(0.05))
    val Control_79_20 = RichCPD(Var_B_value, Var_grade_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Control_79_47 = RichCPD(Var_A_value, Var_grade_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_79_15 = RichCPD(Control_79_20, Control_79_47, Stmt_75_15, 
      (OneOf(true), OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *, *) -> Flip(0.05))
    val Control_83_20 = RichCPD(Var_grade_value, Var_C_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Control_83_47 = RichCPD(Var_D_value, Var_grade_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_83_15 = RichCPD(Control_83_20, Control_83_47, Stmt_79_15, 
      (OneOf(true), OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *, *) -> Flip(0.05))
    val Var_output_4 = RichCPD(Var_output_3, Stmt_83_15, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_85_12 = If(Stmt_83_15, Flip(0.99), Flip(0.05))
    val Ret = If(Var_output_4, Flip(0.99), Flip(0.05))

    //-------------Observation--------------
    Var_scanner.observe(true)
    Var_output.observe(true)

    //-------------Constraint--------------

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println(samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}
