import com.cra.figaro.algorithm.factored.VariableElimination
import com.cra.figaro.language._
import com.cra.figaro.library.compound._

object patch {
  def main(args: Array[String]): Unit = {
    //-------------Semantic--------------
    val Var_scanner = Flip(0.5)
    val Var_output = Flip(0.5)
    val Var_character = Flip( 0.7)
    val Var_remainder = Flip( 0.7)
    val Var_sum = Flip( 0.7)
    val Var_output_1 = If(Var_output, Flip(0.99), Flip(0.05))
    val Var_sum_value = Flip( 0.7)
    val Var_character_value = If(Var_character, Flip(0.99), Flip(0.05))
    val Control_67_15 = If(Var_character_value, Flip(0.99), Flip(0.05))
    val Stmt_67_8 = If(Control_67_15, Flip(0.99), Flip(0.05))
    val Stmt_68_12 = If(Stmt_67_8, Flip(0.99), Flip(0.05))
    val Var_character_value_1 = RichCPD(Var_scanner, Stmt_68_12, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Var_sum_value_1 = RichCPD(Var_character_value_1, Var_sum_value, Stmt_67_8, 
      (OneOf(true), OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *, *) -> Flip(0.05))
    val Control_67_15_1 = If(Var_character_value_1, Flip(0.99), Flip(0.05))
    val Stmt_67_8_1 = If(Control_67_15_1, Flip(0.99), Flip(0.05))
    val Stmt_68_12_1 = If(Stmt_67_8_1, Flip(0.99), Flip(0.05))
    val Var_character_value_2 = RichCPD(Var_scanner, Stmt_68_12_1, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Var_sum_value_2 = RichCPD(Var_character_value_2, Var_sum_value_1, Stmt_67_8_1, 
      (OneOf(true), OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *, *) -> Flip(0.05))
    val Control_67_15_2 = If(Var_character_value_2, Flip(0.99), Flip(0.05))
    val Stmt_67_8_2 = If(Control_67_15_2, Flip(0.99), Flip(0.05))
    val Stmt_68_12_2 = If(Stmt_67_8_2, Flip(0.99), Flip(0.05))
    val Stmt_68_12_3 = If(Stmt_67_8_2, Flip(0.99), Flip(0.05))
    val Stmt_70_14 = If(Stmt_68_12_3, Flip(0.99), Flip(0.05))
    val Var_e = Flip(0.5)
    val Var_character_value_3 = If(Stmt_70_14, Flip(0.99), Flip(0.05))
    val Var_sum_value_3 = RichCPD(Var_character_value_3, Var_sum_value_2, Stmt_67_8_2, 
      (OneOf(true), OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *, *) -> Flip(0.05))
    val Control_67_15_3 = If(Var_character_value_3, Flip(0.99), Flip(0.05))
    val Var_remainder_value = If(Var_sum_value_3, Flip(0.99), Flip(0.05))
    val Var_output_2 = RichCPD(Var_output_1, Var_remainder_value, 
      (OneOf(true), OneOf(true)) -> Flip(0.99),
      (*, *) -> Flip(0.05))
    val Stmt_77_8 = Flip( 0.7)
    val Ret = If(Var_output_2, Flip(0.99), Flip(0.05))

    //-------------Observation--------------
    Var_scanner.observe(true)
    Var_output.observe(true)
    Var_e.observe(true)

    //-------------Constraint--------------

    //-------------Sampling--------------
    val samplePatchValid = VariableElimination(Ret)
    samplePatchValid.start()
    println(samplePatchValid.probability(Ret, true))
    samplePatchValid.kill()
  }
}
