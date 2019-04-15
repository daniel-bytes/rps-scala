import React from 'react';
import AuthedApp from './components/AuthedApp'
import { GoogleAuthService } from './services/GoogleAuthService';
import SessionManager from './services/SessionManager';

interface Props {}

export const App: React.SFC<Props> = (props) => {
  const sessionManager = new SessionManager(
    new GoogleAuthService({
      apiScriptUrl: "https://apis.google.com/js/api.js",
      clientId: "391796029454-blfdr00tme5fj3one35ke54h0q5rosgv.apps.googleusercontent.com",
      scope: "profile email",
      document: document,
      window: window
    })
  )

  return (
    <div className="App">
      <AuthedApp sessionManager={sessionManager} />
    </div>
  )
}

export default App;
