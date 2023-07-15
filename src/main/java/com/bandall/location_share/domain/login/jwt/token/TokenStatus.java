package com.bandall.location_share.domain.login.jwt.token;

public enum TokenStatus {
    TOKEN_VALID,
    TOKEN_EXPIRED,
    TOKEN_IS_BLACKLIST,
    TOKEN_WRONG_SIGNATURE,
    TOKEN_HASH_NOT_SUPPORTED,
    TOKEN_VALIDATION_TRY_FAILED,
    NO_AUTH_HEADER
}
