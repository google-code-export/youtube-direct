package com.google.ytd.dao;

import com.google.ytd.dao.UserAuthTokenDaoImpl.TokenType;
import com.google.ytd.model.UserAuthToken;

public interface UserAuthTokenDao {
  public UserAuthToken getUserAuthToken(String username);

  public void setUserAuthToken(String username, String token);

  public void setUserAuthToken(String username, String token, TokenType tokenType);
}
