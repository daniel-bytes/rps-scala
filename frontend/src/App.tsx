import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import { ActionButtons } from './components/ActionButtons'
import ErrorBox from './components/ErrorBox'
import GameApp from './components/GameApp'
import { IApplicationStore } from './services/ApplicationStore'

interface Props {
  applicationStore?: IApplicationStore
}

@inject('applicationStore')
@observer
export default class App extends Component<Props> {

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
    return this.props.applicationStore!.sessionInitialized 
      ? this.renderApp()
      : this.renderLoading()
  }

  private renderApp() {
    return (
      <div className="container">
        <div className="two-thirds-item">
          <h1 className="title">Rock/Paper/Scissor</h1>
        </div>
        <div className="third-item">
          <ActionButtons />
        </div>
        <div className="half-item">
          { this.props.applicationStore!.subtitle }
        </div>
        <div className="half-item">
          <ErrorBox />
        </div>
        <div className="row">
          { this.props.applicationStore!.loggedIn && <GameApp /> }
        </div>
      </div>
    )
  }

  private renderLoading() {
    return <div className="container">loading session...</div>
  }
}
