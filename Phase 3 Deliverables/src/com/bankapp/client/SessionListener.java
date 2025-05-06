package com.bankapp.client;

import com.bankapp.common.SessionInfo;
public interface SessionListener {
  void onLoginSuccess(SessionInfo session, ConnectionHandler handler);
  void onLoginFailure(String errorMessage);
  /** Called when the server sends a SHUTDOWN message. */
  void onServerShutdown();

  /** Called if the connection is unexpectedly dropped. */
  void onConnectionLost(String message);
}
