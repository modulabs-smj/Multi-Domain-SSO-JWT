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
2. 유저가 복수의 권한(Role)을 가질 수 있도록 수정
3. 관리자 기능 추가
4. 액추에이터 추가

<br>

# API 명세서

1. [LoginController](#logincontroller)
  - [1.1. 계정 생성](#1-계정-생성)
  - [1.2. Email, Password 로그인](#2-email-password-로그인)
  - [1.3. Access 토큰 재발급](#3-access-토큰-재발급)
  - [1.4. 로그아웃](#4-로그아웃)
  - [1.5. 유저 이름 변경](#5-유저-이름-변경)
  - [1.6. 비밀번호 변경](#6-비밀번호-변경)
  - [1.7. 계정 삭제](#7-계정-삭제)
  - [1.8. 이메일 인증 코드 발급](#8-이메일-인증-코드-발급)
  - [1.9. 이메일 인증 코드 제출](#9-이메일-인증-코드-제출)
2. [OAuthController](#oauthcontorller)
  - [2.1. 소셜 로그인](#1-소셜-로그인)
  - [2.2. 계정 삭제](#2-계정-삭제)

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

# OAuthContorller
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
