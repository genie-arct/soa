# 交流群 790566299
# soa
基于java语言的分布式soa架构服务脚手架，方便开发人员快速开发相关soa的接口或者内部服务，支持json、xml协议，同时可自定义.
可使用开发公司web接口或者内部http接口，可用于生产环境。

# 使用技术

- 代码：纯java底层，没有使用任何框架，值得学习下
- 部署容器：Tomcat，也可以改成springboot

# 代码结构

![SOA代码结构](https://github.com/genie-arct/soa/blob/master/doc/1572427198.jpg)

**代码包介绍**

1. base包：主要是soa的初始化包括插件、数据库连接等配置
2. engin包：引擎模块，soa服务协调等
3. handler包：Message消息处理模块
4. protocol包：soa框架协议处理模块

# 功能清单

- 数据库连接池处理
- 配置加载（均可自定义）
- host主机校验相关处理
- 统一日志处理
- DML语言处理
- 白名单处理
- 扩展模块
- 等等，可以自己添加

# 部署步骤
1. 将doc/data 里面的表结构导入进MySql，配置好数据源
2. maven run 打成jar包，放到你需要使用工程中。
3. 将你的工程丢到tomcat中，并启动
4. 访问地址：http://127.0.0.1:8080/gds.dss.sys/gds_soa

# post 参数示例：
{"ServiceFrame":{"BusCode":"TP_BUS_TEST","BusVersion":"v1.0.0","SourceSysId":"Test","RequestDate":"20191029152734","ValideDate":"20191029152734","Sign":"1"},"ServiceContent":{"tripType":"Single","Sfc_Amount":174000,"extra_param1":1,"contactMobile":"","officeNo":""}}

# 联系作者

- 更细节的使用可以关注作者微信公众号：架构师修炼，不仅可以学习本项目，还能学习更多作者一线互联网架构实操，包括不限于分布式、高并发、爬虫、python等，以及会推出更多源码
![soa代码结构](https://github.com/genie-arct/soa/blob/master/doc/qrcode_for_gh_7167f2d74040_344.jpg)

- 有好的思路的同学可以提pr，谢谢
