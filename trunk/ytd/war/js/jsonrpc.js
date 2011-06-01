/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// namespace protection against collision
var jsonrpc = jsonrpc || {};

// JSON-RPC endpoint
var JSON_RPC_URL = "/jsonrpc";

jsonrpc.makeRequest = function(command, params, callback) {  
  var postData = {};
  postData.method = command;
  postData.params = params;
  
  var ajaxCall = {};
  ajaxCall.type = 'POST';
  ajaxCall.contentType = 'application/json';
  ajaxCall.url = JSON_RPC_URL + '?cachebust=' + new Date().getTime();
  ajaxCall.data = JSON.stringify(postData);
  ajaxCall.cache = false;
  ajaxCall.processData = false;
  ajaxCall.error = function(res) {
    document.body.style.cursor = "default";
    callback(res);
  };
  ajaxCall.success = function(res) {
    document.body.style.cursor = "default";
    callback(res);
  };
  
  document.body.style.cursor = "wait";
  jQuery.ajax(ajaxCall);
};