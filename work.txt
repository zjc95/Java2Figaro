
-------------主要内容-------------
对控制依赖的支持
动态跟踪接口
策略接口实现 trace → constraint

动态跟踪的实现：<行, 列, 定值>
参数统计与选择

-------------细节部分-------------
控制语句短路情况
域、数组关联添加

-------------可能添加-------------
使用kiama生成figaro程序
使用kiama进行程序分析

跨函数分析的处理
有副作用的函数调用处理

策略：根据assert语义确定正确性
策略：根据类型确定正确概率
策略：同变量不同域间独立性
策略：根据类结构确定变量与域正确概率关系

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


