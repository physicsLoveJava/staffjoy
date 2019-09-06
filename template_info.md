# 源码自动生成模板 spring-boot

### 概述

* 模板: spring-boot
* 模板使用时间: 2019-09-06 13:08:11

### Docker
* Image: registry.cn-beijing.aliyuncs.com/code-template/spring-boot
* Tag: 20190731
* SHA256: b2d9b97c2b1a6f9b7571d9aad3aab64ce069a8b6a4b226bda0e84bc80c948aca

### 用户输入参数
* repoUrl: "git@code.aliyun.com:68700-university/codx-center.git" 
* needDockerfile: "Y" 
* appName: "app-faraday" 
* operator: "aliyun_1024633" 
* appReleaseContent: "# 
* 请参考: 请参考 
* https://help.aliyun.com/document_detail/59293.html: https://help.aliyun.com/document_detail/59293.html 
* 了解更多关于release文件的编写方式: 了解更多关于release文件的编写方式 
* [NEWLINE][NEWLINE]#: [NEWLINE][NEWLINE]# 
* 构建源码语言类型[NEWLINE]code.language: oracle-jdk1.8[NEWLINE][NEWLINE]# 
* 构建打包使用的打包文件[NEWLINE]build.output: target/app-faraday.jar[NEWLINE][NEWLINE]# 
* Docker镜像构建之后push的仓库地址[NEWLINE]docker.repo: registry.cn-shanghai.aliyuncs.com/codx/center-faraday" 

### 上下文参数
* appName: app-faraday
* operator: aliyun_1024633
* gitUrl: git@code.aliyun.com:68700-university/codx-center.git
* branch: master


### 命令行
	sudo docker run --rm -v `pwd`:/workspace -e repoUrl="git@code.aliyun.com:68700-university/codx-center.git" -e needDockerfile="Y" -e appName="app-faraday" -e operator="aliyun_1024633" -e appReleaseContent="# 请参考 https://help.aliyun.com/document_detail/59293.html 了解更多关于release文件的编写方式 [NEWLINE][NEWLINE]# 构建源码语言类型[NEWLINE]code.language=oracle-jdk1.8[NEWLINE][NEWLINE]# 构建打包使用的打包文件[NEWLINE]build.output=target/app-faraday.jar[NEWLINE][NEWLINE]# Docker镜像构建之后push的仓库地址[NEWLINE]docker.repo=registry.cn-shanghai.aliyuncs.com/codx/center-faraday"  registry.cn-beijing.aliyuncs.com/code-template/spring-boot:20190731

