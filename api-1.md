# UsersController

UsersController


---
## 用户注册

> BASIC

**Path:** /user/register

**Method:** POST

> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | YES |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| userName | string |  |
| password | string |  |
| checkPassword | string |  |

**Request Demo:**

```json
{
  "userName": "",
  "password": "",
  "checkPassword": ""
}
```



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| code | integer |  |
| data | integer |  |
| message | string |  |

**Response Demo:**

```json
{
  "code": 0,
  "data": 0,
  "message": ""
}
```




---
## 用户登录

> BASIC

**Path:** /user/login

**Method:** POST

> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | YES |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| username | string | 用户账号 |
| password | string | 用户密码 |

**Request Demo:**

```json
{
  "username": "",
  "password": ""
}
```



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| code | integer |  |
| data | object |  |
| &ensp;&ensp;&#124;─userId | integer | id |
| &ensp;&ensp;&#124;─username | string | 账号 |
| &ensp;&ensp;&#124;─userAvatar | string | 用户头像 |
| &ensp;&ensp;&#124;─userProfile | string | 用户简介 |
| &ensp;&ensp;&#124;─userRole | string | 用户角色：user/admin/ban |
| &ensp;&ensp;&#124;─createTime | string | 创建时间 |
| &ensp;&ensp;&#124;─updateTime | string | 更新时间 |
| &ensp;&ensp;&#124;─token | string | token |
| message | string |  |

**Response Demo:**

```json
{
  "code": 0,
  "data": {
    "userId": 0,
    "username": "",
    "userAvatar": "",
    "userProfile": "",
    "userRole": "",
    "createTime": "",
    "updateTime": "",
    "token": ""
  },
  "message": ""
}
```




---
## 用户注销

> BASIC

**Path:** /user/logout

**Method:** POST

> REQUEST



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| code | integer |  |
| data | boolean |  |
| message | string |  |

**Response Demo:**

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```




---
## 获取当前登录用户

> BASIC

**Path:** /user/get/login

**Method:** GET

> REQUEST



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| code | integer |  |
| data | object |  |
| &ensp;&ensp;&#124;─userId | integer | id |
| &ensp;&ensp;&#124;─username | string | 账号 |
| &ensp;&ensp;&#124;─userAvatar | string | 用户头像 |
| &ensp;&ensp;&#124;─userProfile | string | 用户简介 |
| &ensp;&ensp;&#124;─userRole | string | 用户角色：user/admin/ban |
| &ensp;&ensp;&#124;─createTime | string | 创建时间 |
| message | string |  |

**Response Demo:**

```json
{
  "code": 0,
  "data": {
    "userId": 0,
    "username": "",
    "userAvatar": "",
    "userProfile": "",
    "userRole": "",
    "createTime": ""
  },
  "message": ""
}
```




---
## 获取所有用户信息

> BASIC

**Path:** /user/list/all

**Method:** GET

> REQUEST

**Query:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| current |  | NO | 当前页号 |
| pageSize |  | NO | 页面大小 |
| sortField |  | NO | 排序字段 |
| sortOrder |  | NO | 排序顺序（默认降序） |
| userId |  | NO | id |
| username |  | NO | 用户账户 |
| profile |  | NO | 简介 |
| userRole |  | NO | 用户角色：user/admin/ban |



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| code | integer |  |
| data | object |  |
| &ensp;&ensp;&#124;─records | array |  |
| &ensp;&ensp;&ensp;&ensp;&#124;─ | object |  |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─userId | integer | id |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─username | string | 账号 |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─userAvatar | string | 用户头像 |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─userProfile | string | 用户简介 |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─userRole | string | 用户角色：user/admin/ban |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─createTime | string | 创建时间 |
| &ensp;&ensp;&#124;─total | integer |  |
| &ensp;&ensp;&#124;─size | integer |  |
| &ensp;&ensp;&#124;─current | integer |  |
| &ensp;&ensp;&#124;─orders | array |  |
| &ensp;&ensp;&ensp;&ensp;&#124;─ | object |  |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─column | string |  |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─asc | boolean |  |
| &ensp;&ensp;&#124;─optimizeCountSql | boolean |  |
| &ensp;&ensp;&#124;─searchCount | boolean |  |
| &ensp;&ensp;&#124;─optimizeJoinOfCountSql | boolean |  |
| &ensp;&ensp;&#124;─maxLimit | integer |  |
| &ensp;&ensp;&#124;─countId | string |  |
| message | string |  |

**Response Demo:**

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "userId": 0,
        "username": "",
        "userAvatar": "",
        "userProfile": "",
        "userRole": "",
        "createTime": ""
      }
    ],
    "total": 0,
    "size": 0,
    "current": 0,
    "orders": [
      {
        "column": "",
        "asc": false
      }
    ],
    "optimizeCountSql": false,
    "searchCount": false,
    "optimizeJoinOfCountSql": false,
    "maxLimit": 0,
    "countId": ""
  },
  "message": ""
}
```




---
## 分页获取用户信息

> BASIC

**Path:** /user/list/page

**Method:** GET

> REQUEST

**Query:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| current |  | NO | 当前页号 |
| pageSize |  | NO | 页面大小 |
| sortField |  | NO | 排序字段 |
| sortOrder |  | NO | 排序顺序（默认降序） |
| userId |  | NO | id |
| username |  | NO | 用户账户 |
| profile |  | NO | 简介 |
| userRole |  | NO | 用户角色：user/admin/ban |



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| code | integer |  |
| data | object |  |
| &ensp;&ensp;&#124;─records | array |  |
| &ensp;&ensp;&ensp;&ensp;&#124;─ | object |  |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─userId | integer | id |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─username | string | 账号 |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─userAvatar | string | 用户头像 |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─userProfile | string | 用户简介 |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─userRole | string | 用户角色：user/admin/ban |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─createTime | string | 创建时间 |
| &ensp;&ensp;&#124;─total | integer |  |
| &ensp;&ensp;&#124;─size | integer |  |
| &ensp;&ensp;&#124;─current | integer |  |
| &ensp;&ensp;&#124;─orders | array |  |
| &ensp;&ensp;&ensp;&ensp;&#124;─ | object |  |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─column | string |  |
| &ensp;&ensp;&ensp;&ensp;&ensp;&ensp;&#124;─asc | boolean |  |
| &ensp;&ensp;&#124;─optimizeCountSql | boolean |  |
| &ensp;&ensp;&#124;─searchCount | boolean |  |
| &ensp;&ensp;&#124;─optimizeJoinOfCountSql | boolean |  |
| &ensp;&ensp;&#124;─maxLimit | integer |  |
| &ensp;&ensp;&#124;─countId | string |  |
| message | string |  |

**Response Demo:**

```json
{
  "code": 0,
  "data": {
    "records": [
      {
        "userId": 0,
        "username": "",
        "userAvatar": "",
        "userProfile": "",
        "userRole": "",
        "createTime": ""
      }
    ],
    "total": 0,
    "size": 0,
    "current": 0,
    "orders": [
      {
        "column": "",
        "asc": false
      }
    ],
    "optimizeCountSql": false,
    "searchCount": false,
    "optimizeJoinOfCountSql": false,
    "maxLimit": 0,
    "countId": ""
  },
  "message": ""
}
```




---
## 更新用户信息

> BASIC

**Path:** /user/update

**Method:** POST

> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | YES |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| userId | integer | id |
| username | string | 用户昵称 |
| userAvatar | string | 用户头像 |
| profile | string | 简介 |
| userRole | string | 用户角色：user/admin/ban |

**Request Demo:**

```json
{
  "userId": 0,
  "username": "",
  "userAvatar": "",
  "profile": "",
  "userRole": ""
}
```



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| code | integer |  |
| data | boolean |  |
| message | string |  |

**Response Demo:**

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```




---
## 重置密码

> BASIC

**Path:** /user/reset-password

**Method:** POST

> REQUEST

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| Content-Type | application/json | YES |  |

**Request Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| userId | integer | 用户ID（必须） |
| oldPassword | string | 原始密码（旧密码） |
| newPassword | string | 新密码（新密码） |
| checkPassword | string | 确认新密码 |

**Request Demo:**

```json
{
  "userId": 0,
  "oldPassword": "",
  "newPassword": "",
  "checkPassword": ""
}
```



> RESPONSE

**Headers:**

| name | value | required | desc |
| ------------ | ------------ | ------------ | ------------ |
| content-type | application/json;charset=UTF-8 | NO |  |

**Body:**

| name | type | desc |
| ------------ | ------------ | ------------ |
| code | integer |  |
| data | boolean |  |
| message | string |  |

**Response Demo:**

```json
{
  "code": 0,
  "data": false,
  "message": ""
}
```



