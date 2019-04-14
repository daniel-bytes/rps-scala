import React, { Component } from 'react'
import { GoogleAuthButton } from './GoogleAuthButton'
import { User } from '../models/UserModels'
import SessionManager from '../services/SessionManager'
import GameApp from './GameApp'
import GameService from '../services/GameService';


interface Props {
  sessionManager: SessionManager
}

interface State {
  isReady: Boolean
  isSignedIn: Boolean
  user: User | null
}

export default class AuthedApp extends Component<Props, State> {
  state: State = {
    isReady: false,
    isSignedIn: false,
    user: null
  }

  async onSignInClick(evt: React.MouseEvent<HTMLElement>) {
    const session = await this.props.sessionManager.requestGoogleTokenAsync()
    
    this.setState({
      isReady: true,
      isSignedIn: true,
      user: session.user
    })
  }

  onSignOutClick(evt: React.MouseEvent<HTMLElement>) {
    this.props.sessionManager.clearSessionState()

    this.setState({
      isSignedIn: false,
      user: null
    })
  }

  async componentDidMount() {
    await this.props.sessionManager.initialize()
    const sessionState = await this.props.sessionManager.validateSessionAsync()
    this.setState({ isReady: true })

    if (sessionState) {
      this.setState({ 
        isSignedIn: true, 
        user: sessionState.user
      })
    }
  }

  render() {
    if (this.state.isReady) {
      return <div>
        <GoogleAuthButton 
          isReady={ this.state.isReady }
          isSignedIn={ this.state.isSignedIn }
          onSignInClick={ this.onSignInClick.bind(this) }
          onSignOutClick={ this.onSignOutClick.bind(this) }
        />

        { this.state.user && this.state.isReady && 
          <GameApp 
            user={ this.state.user } 
            gameService={ new GameService(this.props.sessionManager) }
          />
        }
      </div>
    } else {
      return <div>loading...</div>
    }
  }
}
