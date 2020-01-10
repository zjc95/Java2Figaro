### 待办事项

- [] 实现trace 到 figaro 程序的转换 (__2020.1.31__)
    - [] 对控制依赖的支持 ✅
    - [] 控制语句短路情况 ✅
    - [] 域和对应object 之间的关联 ✅
    - [x] 数组关联添加（可以作为一个object处理）

- [] 实现figaro 程序编译及执行 (__2020.2.14__)

- [] 应用候选补丁,实现程序trace (__2020.3.10__)

- [] 添加其他策略 (__2020.2.28__)
  - [] 策略：根据assert语义确定正确性
  - [] 策略：根据类型确定正确概率
  - [] 策略：同变量不同域间独立性
  - [] 策略：根据类结构确定变量与域正确概率关系

- [] 参数统计与选择（最后实现，目前可以人工定义）

- [] 初步实验结果（__2020.3.20__)

----------------------------------------------------

-------------主要内容-------------

动态跟踪接口

策略接口实现 trace → constraint

动态跟踪的实现：<行, 列, 定值>

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

------------git 命令--------------

添加全部修改 git add .

添加文件修改 git add $filepath

提交 git commit -m "$Message"

上传github git push -u origin master

与上版本对比 git diff HEAD -- $filepath


