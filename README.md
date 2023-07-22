# LoginController API Documentation

## Endpoints

### 1. Create Account

- **URL:** `/api/account/create`
- **Method:** `POST`
- **RequestBody:**

  ```json
  {
    "email": "user@example.com",
    "username": "your_username",
    "password": "your_password"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "loginType": "email",
    "email": "user@example.com",
    "username": "your_username"
  }
  ```

### 2. Authenticate (Auth) Account

- **URL:** `/api/account/auth`
- **Method:** `POST`
- **RequestBody:**

  ```json
  {
    "email": "user@example.com",
    "password": "your_password"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "accessToken": "access_token_string",
    "refreshToken": "refresh_token_string"
  }
  ```

### 3. Refresh Token

- **URL:** `/api/account/refresh`
- **Method:** `POST`
- **RequestBody:**

  ```json
  {
    "accessToken": "old_access_token_string",
    "refreshToken": "old_refresh_token_string"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "accessToken": "new_access_token_string",
    "refreshToken": "new_refresh_token_string"
  }
  ```

### 4. Logout

- **URL:** `/api/account/logout`
- **Method:** `POST`
- **Headers:**

    - `Authorization:` `Bearer access_token_string`
- **RequestBody:**

  ```json
  {
    "refreshToken": "your_refresh_token_string"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "message": "Logout Success. Bye ~"
  }
  ```

### 5. Update Username

- **URL:** `/api/account/update-username`
- **Method:** `POST`
- **Headers:**

    - `Authorization:` `Bearer access_token_string`
- **RequestBody:**

  ```json
  {
    "username": "new_username"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "email": "user@example.com",
    "username": "new_username"
  }
  ```

### 6. Update Password

- **URL:** `/api/account/update-password`
- **Method:** `POST`
- **Headers:**

    - `Authorization:` `Bearer access_token_string`
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
    "message": "OK"
  }
  ```

### 7. Delete Account

- **URL:** `/api/account/delete`
- **Method:** `POST`
- **Headers:**

    - `Authorization:` `Bearer access_token_string`
- **RequestBody:**

  ```json
  {
    "password": "users_password"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "message": "OK"
  }
  ```

### 8. Get Verify Email

- **URL:** `/api/email-verification`
- **Method:** `GET`
- **RequestBody:**

  ```json
  {
    "email": "user@example.com"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "message": "OK"
  }
  ```

### 9. Verify Email

- **URL:** `/api/email-verification`
- **Method:** `POST`
- **RequestBody:**

  ```json
  {
    "code": "verification_code"
  }
  ```

- **Success Response:** (HTTP Status 200)

  ```json
  {
    "message": "OK"
  }
```

