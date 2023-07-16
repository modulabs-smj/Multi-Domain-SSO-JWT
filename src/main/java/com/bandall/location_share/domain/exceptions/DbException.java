package com.bandall.location_share.domain.exceptions;

import org.springframework.dao.DataAccessException;

public class DbException extends DataAccessException {
    public DbException(String msg) {
        super(msg);
    }

    public DbException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
