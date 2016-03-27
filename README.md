# FileManager
## 前言
前台使用的是使用angularjs + bootstrap写的一个在线文件管理系统， [这里](https://github.com/joni2back/angular-filemanager)是github地址, 后台使用的是SpringMVC。
写本应用的目的主要是为了在使用Linux时为电脑和手机之间的文件传输提供一个中介。

## 程序功能
* 文件上传下载（上传使用的是[jquery-upload-file](https://github.com/hayageek/jquery-upload-file)）
* 手机扫码快速打开网页
* 图片预览
* 文本文件在线编辑
* 文件/文件夹重名
* 文件/文件夹删除

## 程序预览

![主界面](http://7xqp5u.com1.z0.glb.clouddn.com/file-manager01.jpg)

![文件上传](http://7xqp5u.com1.z0.glb.clouddn.com/file-manager02.jpg)

![文件上传完成](http://7xqp5u.com1.z0.glb.clouddn.com/file-manager03.jpg)

![二维码](http://7xqp5u.com1.z0.glb.clouddn.com/file-manager04.jpg)

![手机端](http://7xqp5u.com1.z0.glb.clouddn.com/file-manager05.jpg)

## 其他说明
### 运行环境
JDK1.7+

### Cpdector.jar
cpdector.jar 位于WEB-INF/lib下， 如果项目没有自动导入jar包， 右击该jar包，选择Add as Library...

### 配置Tomcat的URIEncoding属性
修改tomcat下的conf/server.xml文件
找到如下代码：
```xml
<Connector port="8080" protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" />
```
这段代码规定了Tomcat监听HTTP请求的端口号等信息。
可以在这里添加一个属性：URIEncoding，将该属性值设置为UTF-8，即可让Tomcat（默认ISO-8859-1编码）以UTF-8的编码处理get请求。
修改完成后：
```xml
<Connector port="8080"  protocol="HTTP/1.1" connectionTimeout="20000" redirectPort="8443" URIEncoding="UTF-8" /> 
```
