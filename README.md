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
