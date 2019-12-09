#### svn的提交者只有用户名而没有邮箱，需要进行转换
新建`user.txt`文件，写入如下内容，一个用户一行
```
svn-username = git-username <git-username@email.domain>
```

#### 导出SVN项目至本地
假定svn的项目名称为test，在上述步骤`user.txt`路径下新建`test`目录，进入git bash命令行
```
# 替换命令行中的svnurl
git svn clone svnurl --no-metadata --authors-file=user.txt test
```

#### 进入`test`目录，关联git远程仓库
```
cd test
# 替换命令行中的giturl
git remote add origin giturl
```

#### 可选项：配置当前目录的用户名和邮箱
```
git config --local user.name "git-username"
git config --local user.email "git-username@email.domain"
```

#### 提交记录至git
```
# 提交记录之前，先拉取远程git项目文件(README.md)，否则直接提交会报错
git pull --rebase origin master
# 拉取成功后，执行push命令
git push -u origin master
```