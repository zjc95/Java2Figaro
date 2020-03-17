### 待办事项

- 实现trace 到 figaro 程序的转换 (__2020.1.31__)
    - 对控制依赖的支持 ✅
    - 控制语句短路情况 ✅
    - 动态跟踪接口 ✅
    - 策略接口 ✅
    - 域和对应object 之间的关联 ✅
    - 数组关联添加（可以作为一个object处理）

- 应用候选补丁,实现程序trace (__2020.2.20__)
    - USE RET ASSIGN 插装 ✅
    - CONTROLEXPR 插装 ✅
    - DEF ENTRY 插装 ✅
    - 插装后类型恢复 ✅
    - 自动编译执行trace (Test) ✅
	
- 对数据集编译运行的支持 (__2020.2.24__) 
	- 实现对生成的 Patch 的接口支持 ✅
	- 实现对 Patch 的 Trace 自动编译和执行 ✅
	- 实现对 Trace 的自动获取 ✅
	- 实现 Figaro 程序编译及执行 ✅
	
- 添加其他策略 (__2020.2.28__)
  - 策略：执行到判断语句 a>b 的时候，若 a==b 则认为该判断语句可能出错 ✅ 
  - 策略：在非循环结构中对同一变量多次赋值，则认为该变量可能出错 ✅
  - 策略：若出口变量未关联全部入口变量，则认为出口变量可能出错 ✅
  - 策略：若对某变量使用常数赋值，则该变量可能出错 ✅
  - 策略：根据类结构确定变量与域正确概率关系 ✅

- 初步实验结果（__2020.3.10__)
  - 根据Genpat的数据集提取FP修复 ✅
  - 根据patch进行实验 ✅

- 参数统计与选择（最后实现，目前可以人工定义）

----------------------------------------------------
### 流程

- 通过缺陷程序和测试生成修复补丁

- 应用候选补丁生成 Trace

- 通过 Trace 和 源程序 生成 Figaro 程序

- 编译并运行 Figaro程序 获取推导结果

----------------------------------------------------
### BUG

- += -= 未关联左侧变量 ✅

- 静态调用的类名被作为use ✅

-------------可能添加-------------

使用kiama生成figaro程序

使用kiama进行程序分析

跨函数分析的处理

有副作用的函数调用处理

多patch构造网络 (效率 & 正确性证明)

------------资料记录-----------------

kiama scala库

scala解析 https://scalameta.org/docs/trees/guide.html#construct-trees

c简短程序 Learning to Encode and Classify Test Executions

AST: GenPat

APR tools: astor

Dump: StateBasedFL StateCoverLocator

java -cp .\btrace-client.jar;.\tools.jar com.sun.btrace.compiler.Compiler AllLines.java
-Djvmargs=-javaagent:D:\program\workspace\Java2FigaroData\tmp\btrace\btrace-agent.jar=noserver,debug=true,scriptOutputFile=D:\program\workspace\Java2FigaroData\tmp\btrace\output.txt,script=D:\program\workspace\Java2FigaroData\tmp\btrace\AllLines.class

------------git命令--------------

添加全部修改 git add .

添加文件修改 git add $filepath

提交 git commit -m "$Message"

上传github git push -u origin master

与上版本对比 git diff HEAD -- $filepath


