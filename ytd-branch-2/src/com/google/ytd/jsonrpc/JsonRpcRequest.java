package com.google.ytd.jsonrpc;

import java.util.Map;

import com.google.gson.annotations.Expose;

public class JsonRpcRequest {
  @Expose
  private String method = null;
  @Expose
  private Map<String,String> params = null;

  public JsonRpcRequest() {
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public String getMethod() {
    return method;
  }

  public void setParams(Map<String,String> params) {
    this.params = params;
  }

  public Map<String,String> getParams() {
    return params;
  }

  public String getParam(String key) {
    return params.get(key);
  }
}
