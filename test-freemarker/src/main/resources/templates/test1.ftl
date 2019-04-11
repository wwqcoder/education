<!DOCTYPE html>
<html>
<head>
    <meta charset="utf‐8">
    <title>Hello World!</title>
</head>
<body>
Hello ${name}!
<br/>
遍历数据模型中list的学生信息（stu）
<table border="1">
    <tr>
        <th>序号</th>
        <th>姓名</th>
        <th>年龄</th>
        <th>金额</th>
        <th>出生日期</th>
    </tr>
    <#if stus??>
        <#list stus as stu>
            <tr>
                <td>${stu_index+1}</td>
                <td <#if stu.name=='小明'>style="background-color: red" </#if>>${stu.name}</td>
                <td>${stu.age}</td>
                <td <#if stu.money gt 300>style="background-color: red"</#if>>${stu.money}</td>
                <td>${stu.birthday?string("yyyy-MM-dd")}</td>
            </tr>
        </#list>
        list的大小：${stus?size}
    </#if>
</table>
<br>
遍历数据模型中的stuMap(map)1.'[]'中填写map的key  2.map.key
<br>
姓名： ${(stuMap['stu1'].name)!""}<br/>
年龄： ${(stuMap['stu1'].age )!""}<br/>
姓名： ${(stuMap.stu1.name   )!""}<br/>
年龄： ${(stuMap.stu1.age    )!""}<br/>

遍历map中的key，stuMap?keys就是key列表(list)
<#list stuMap?keys as k>
    姓名： ${stuMap[k].name}<br/>
    年龄： ${stuMap[k].age}<br/>
</#list>
<br/>
${point?c}
<br/>
<#assign text="{'bank':'工商银行','account':'10101920201920212'}" />
<#assign data=text?eval />
开户行：${data.bank}  账号：${data.account}

</body>
</html>