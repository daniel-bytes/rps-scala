import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import { ActionButtons } from './ActionButtons'
import { User } from '../models/UserModels'
import { ISessionService } from '../services/SessionService'
import { IApplicationStore } from '../services/ApplicationStore'
import GameApp from './GameApp'

interface Props {
  sessionStore?: ISessionService
  applicationStore?: IApplicationStore
}

@inject('applicationStore')
@observer
export default class AuthedApp extends Component<Props> {

  async componentDidMount() {
    await this.props.applicationStore!.initializeSessionStoreAsync()
  }

  async onSignInClick() {
    await this.props.applicationStore!.loginAsync()
  }

  onSignOutClick() {
    this.props.applicationStore!.logout()
  }

  render() {
    if (this.props.applicationStore!.sessionInitialized) {
      return <div className="container">
        <h1 className="title">Rock/Paper/Scissor</h1>
        <ActionButtons />

        { this.props.applicationStore!.loggedIn && <GameApp /> }
      </div>
    } else {
      return <div className="container">loading session...</div>
    }
  }
}
