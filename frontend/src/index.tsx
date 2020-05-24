import React from 'react'
import ReactDOM from 'react-dom'
import { configure } from "mobx"
import { Provider } from 'mobx-react'
import './handdrawn.css'
import './index.css'
import App from './App'
import { GoogleAuthService } from './services/GoogleAuthService'
import { SessionService } from './services/SessionService'
import { GameService } from './services/GameService'
import { ApplicationStore} from './services/ApplicationStore'

configure({ enforceActions: 'observed' })

// create service/store instances
const sessionStore = new SessionService(
  new GoogleAuthService({
    apiScriptUrl: "https://apis.google.com/js/api.js",
    clientId: "391796029454-blfdr00tme5fj3one35ke54h0q5rosgv.apps.googleusercontent.com",
    scope: "profile email",
    document: document,
    window: window
  })
)

const gameStore = new GameService(sessionStore)

const stores = {
  applicationStore: new ApplicationStore(gameStore, sessionStore)
}

ReactDOM.render(
  <Provider {...stores}>
    <App />
  </Provider>, 
  document.getElementById('root')
)
