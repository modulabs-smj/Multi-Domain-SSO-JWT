<div align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&height=250&color=80ea6e&fontColor=363636&text=%EC%8A%A4%ED%94%84%EB%A7%81%20JWT%20%EB%A1%9C%EA%B7%B8%EC%9D%B8%20%EC%84%9C%EB%B2%84" alt="header"/>
</div>

<div align="center">
    JWT Access, Refresh 토큰을 이용한 로그인 서버
</div>

## 🛠️ 기술 스택 🛠️

<div align="center">
    <img src="https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
    <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
    <img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
    <br>
    <img src="https://img.shields.io/badge/redis-DC382D?style=for-the-badge&logo=redis&logoColor=white">
    <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
    <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=white">
    <img src="https://img.shields.io/badge/thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white">

</div>

<br>

## 🧰 개발 도구 🧰

<div align="center">
    <img src="https://img.shields.io/badge/intellijidea-000000?style=for-the-badge&logo=intellijidea&logoColor=white">
    <img src="https://img.shields.io/badge/postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white">
    <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">    
    <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white">
</div>

## 📆 구현 예정 기능 📆
1. 네이버, 구글 OAuth 추가
2. 액추에이터 추가

<br>

<p align="center">
  <a href="https://github.com/bandall/location_share_flutter"><strong>🔗 로그인 테스트용 Flutter 코드 🔗</strong></a>
</p>


# API 명세서

1. [JWT 토큰](#JWT-토큰)
   - [1.1 Access Token](#access-token)
   - [1.2 Refresh Token](#refresh-token)
2. [LoginController](#logincontroller)
   - [2.1. 계정 생성](#1-계정-생성)
   - [2.2. Email, Password 로그인](#2-email-password-로그인)
   - [2.3. Access 토큰 재발급](#3-access-토큰-재발급)
   - [2.4. 로그아웃](#4-로그아웃)
   - [2.5. 유저 이름 변경](#5-유저-이름-변경)
   - [2.6. 전화번호 변경](#6-전화번호-변경)
   - [2.7. 비밀번호 변경](#7-비밀번호-변경)
   - [2.8. 계정 삭제](#8-계정-삭제)
   - [2.9. 이메일 인증 코드 발급](#9-이메일-인증-코드-발급)
   - [2.10. 이메일 인증 코드 제출](#10-이메일-인증-코드-제출)
   - [2.11. 비밀번호 재설정 이메일 코드 발급](#11-비밀번호-재설정-이메일-코드-발급)
   - [2.12. 비밀번호 재설정](#12-비밀번호-재설정)
3. [OAuthController](#oauthcontroller)
   - [3.1. 소셜 로그인](#1-소셜-로그인)
   - [3.2. 계정 삭제](#2-계정-삭제)
4. [AdminController](#admincontroller)
   - [4.1. 회원 정보 조회](#1-회원-정보-조회)
   - [4.2. 특정 회원 정보 조회](#2-특정-회원-정보-조회)
   - [4.3. 권한 종류 조회](#3-권한-종류-조회)
   - [4.4. 회원 권한 수정](#4-회원-권한-수정)
5. [Response Status Code](#response-status-code)
   - [5.1. 응답 JSON 형식](#응답-json-형식)
   - [5.2. 코드 정보](#코드-정보)
# JWT 토큰
## 기본 구조
### Access Token
**Header**
```
{
  "alg": "HS512"
}
```
**PAYLOAD**
```
{
  "sub": "[email]",
  "auth": "[role]",
  "username": "[username]",
  "tokenId": "[token Id]",
  "exp": token_expire_time
}
```
**VERIFY SIGNATURE**
```
HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret key
)
```

### Refresh Token
**Header**
```
{
  "alg": "HS512"
}
```
**PAYLOAD**
```
{
  "exp": token_expire_time,
  "sub": "[email]",
  "tokenId": "[token Id]"
}
```
**VERIFY SIGNATURE**
```
HMACSHA512(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secret key
)
```

# LoginController
## Endpoints
### 1. 계정 생성

- **URL:** `/api/account/create`
- **Method:** `POST`
- **RequestBody:**

  ```json
  {
    "email" : "[email]",
    "password": "[password]",
    "username" : "[username]"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": {
        "loginType": "EMAIL_PW",
        "username": "[username]",
        "email": "[email]"
    }
  }
  ```

### 2. Email, Password 로그인

- **URL:** `/api/account/auth`
- **Method:** `POST`
- **RequestBody:**

  ```json
  {
    "loginType": "EMAIL_PW",
    "email": "[email]",
    "password": "[password]"
  }
  ```
 
- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": {
        "accessToken": "[access_token_string]",
        "refreshToken": "[refresh_token_string]",
        "accessTokenExpireTime": "[access_token_expire_time]",
        "refreshTokenExpireTime": "[refresh_token_expireTime]",
        "ownerEmail": "[email]",
        "tokenId": "[token id]"
    }
  }
  ```
**참고**  
이메일 인증을 완료하지 않았을 경우 이메일 인증 오류가 발생합니다.

### 3. Access 토큰 재발급

- **URL:** `/api/account/refresh`
- **Method:** `POST`
- **RequestBody:**

  ```json
  {
    "accessToken": "[old_access_token_string]",
    "refreshToken": "[old_refresh_token_string]"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": {
        "accessToken": "[new_access_token_string]",
        "refreshToken": "[new_refresh_token_string]",
        "accessTokenExpireTime": "[new_access_token_expire_time]",
        "refreshTokenExpireTime": "[new_refresh_token_expireTime]",
        "ownerEmail": "[email]",
        "tokenId": "[new_token id]"
    }
  }
  ```
  
**참고**  
Token ID가 같은 토큰 쌍만 재발급이 가능합니다.

### 4. 로그아웃

- **URL:** `/api/account/logout`
- **Method:** `POST`
- **Headers:**

    - `Authorization:` `Bearer [access_token_string]`
- **RequestBody:**

  ```json
  {
    "refreshToken": "[refresh_token_string]"
  }
  ```
  Access 토큰 만료 시 400 ERROR
- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": "Logout Success. Bye ~"
  }
  ```

### 5. 유저 이름 변경

- **URL:** `/api/account/update-username`
- **Method:** `POST`
- **Headers:**

    - `Authorization:` `Bearer [access_token_string]`
- **RequestBody:**

  ```json
  {
    "username": "[new_username]"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": {
        "email": "[email]",
        "username": "[new_username]"
    }
  }
  ```

### 6. 비밀번호 변경

- **URL:** `/api/account/update-password`
- **Method:** `POST`
- **Headers:**

    - `Authorization:` `Bearer [access_token_string]`
- **RequestBody:**

  ```json
  {
    "oldPassword": "old_password",
    "newPassword": "new_password"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": "OK"
  }
  ```

### 7. 계정 삭제

- **URL:** `/api/account/delete`
- **Method:** `POST`
- **Headers:**

    - `Authorization:` `Bearer access_token_string`
- **RequestBody:**

  ```json
  {
    "password": "[users_password]"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": "OK"
  }
  ```

### 8. 이메일 인증 코드 발급

- **URL:** `/api/email-verification?email=[email]`
- **Method:** `GET`

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": "OK"
  }
  ```

### 9. 이메일 인증 코드 제출

- **URL:** `/api/email-verification`
- **Method:** `POST`
- **RequestBody:**

  ```json
  {
    "email": "[email]",
    "code": "[verification_code]"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": "OK"
  }
  ```

### 10. 비밀번호 재설정 이메일 코드 발급

- **URL:** `/api/account/find-password?email=[email]`
- **Method:** `GET`
- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": "OK"
  }
  ```

### 11. 비밀번호 재설정

- **URL:** `/api/account/find-password`
- **Method:** `POST`
- **RequestBody:**

  ```json
  {
    "email": "[email]",
    "code": "[verification_code]",
    "newPassword": "[new_password]"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": "OK"
  }
  ```

# OAuthController
## Endpoints
### 1. 소셜 로그인

- **URL:** `/oauth/login/kakao?code=[kakao_access_token]`
- **Method:** `GET`
- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": {
        "accessToken": "[access_token_string]",
        "refreshToken": "[refresh_token_string]",
        "accessTokenExpireTime": "[access_token_expire_time]",
        "refreshTokenExpireTime": "[refresh_token_expireTime]",
        "ownerEmail": "[email]",
        "tokenId": "[token id]"
    }
  }
  ```

### 2. 계정 삭제

- **URL:** `/oauth/unlink/kakao`
- **Method:** `POST`
- **Headers:**
  - `Authorization:` `Bearer [access_token_string]`
- **RequestBody:**

  ```json
  {
    "code": "[kakao_access_token]"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "httpStatus": "OK",
    "code": 200,
    "data": "OK"
  }
  ```

# AdminController
## Endpoints
## 1. 회원 정보 조회

- **URL:** `/api/admin/members`
- **Method:** `GET`
- **Request Parameters:**

  | Parameter | Description | Required | Default |
  | --- | --- | --- | --- |
  | page | Page number | Optional | 0 |
  | size | Number of members per page | Optional | 10 |

- **Success Response:** (HTTP Status 200)

```json
  {
    "httpStatus": "OK",
    "data": "<PageDto<MemberInfo>>"
  }
```

## 2. 특정 회원 정보 조회

- **URL:** `/api/admin/member`
- **Method:** `GET`
- **Request Parameters:**

  | Parameter | Description | 
  | --- | --- |
  | email | Email of the member to retrieve |

- **Success Response:** (HTTP Status 200)

```json
  {
    "httpStatus": "OK",
    "data": "<MemberInfo>"
  }
```

## 3. 권한 종류 조회

- **URL:** `/api/admin/roles`
- **Method:** `GET`
- **Success Response:** (HTTP Status 200)

```json
  {
    "httpStatus": "OK",
    "data": "<RoleInfo>"
  }
```
- 서버에 어떤 권한이 있는지 조회합니다.

## 4. 회원 권한 수정

- **URL:** `/api/admin/member/role`
- **Method:** `POST`
- **RequestBody:**

```json
  {
    "email": "<Member email>",
    "role": "<Role>",
    "action": "<Action>"
  }
```

| Parameter | Description | 
| --- | --- |
| email | Email of the member to Modify Role |
| role  | role to add or remove |
| action | `ADD_ROLE` or `REMOVE_ROLE` |

- **Success Response:** (HTTP Status 200)

```json
  {
    "httpStatus": "OK",
    "data": "OK"
  }
```


# Response Status Code
## 응답 JSON 형식
### 성공 응답
```
{
  "httpStatus": "[http status code]",
  "code": 200,
  "data": "{ data_json }"
}
```

### 오류 응답
```
{
  "httpStatus": "[http error status code]",
  "code": error_code,
  "data": "[error msg]"
}
```

## 코드 정보
### 일반 상태 코드
| Code  | Name                 | Description         |
|-------|----------------------|---------------------|
| `200` | `OK`                 | 요청이 성공적으로 처리되었습니다.  |
| `404` | `URL_NOT_FOUND`      | 요청한 URL을 찾을 수 없습니다. |
| `410` | `EMAIL_NOT_VERIFIED` | 이메일 인증이 완료되지 않았습니다. |
| `420` | `WRONG_PARAMETER`    | 잘못된 파라미터가 입력되었습니다.  |
| `430` | `LOGIN_FAILED`       | 로그인에 실패하였습니다.       |
| `500` | `SERVER_ERROR`       | 서버 내부 오류가 발생하였습니다.  |


### 토큰 상태 코드

| Code   | Name                          | Description                   |
|--------|-------------------------------|-------------------------------|
| `4011` | `TOKEN_EXPIRED`               | 토큰이 만료되었습니다.                  |
| `4012` | `TOKEN_IS_BLACKLIST`          | 토큰이 로그아웃 되어 블랙리스트에 등록되어 있습니다. |
| `4013` | `TOKEN_WRONG_SIGNATURE`       | 토큰의 서명이 잘못되었습니다.              |
| `4014` | `TOKEN_HASH_NOT_SUPPORTED`    | 지원하지 않는 해시 알고리즘이 사용된 토큰입니다.   |
| `4015` | `NO_AUTH_HEADER`              | Authentication 헤더가 없습니다.      |
| `4016` | `TOKEN_VALIDATION_TRY_FAILED` | 토큰 검증 시도가 실패했습니다.             |
