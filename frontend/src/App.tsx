import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import { ActionButtons } from './components/ActionButtons'
import ErrorBox from './components/ErrorBox'
import Notifications from './components/Notifications'
import GameApp from './components/GameApp'
import Instructions from './components/Instructions'
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
    await this.props.applicationStore!.loginGoogleAsync()
  }

  onSignOutClick() {
    this.props.applicationStore!.logout()
  }

  render() {
    if (this.props.applicationStore!.sessionInitialized) {
      return this.renderApp()
    } else if (this.props.applicationStore!.apiError) {
      return this.renderError()
    } else {
      return this.renderLoading()
    }
  }

  private renderApp() {
    return (
      <div id="container">
        <header className="container">
          <h1>Rock/Paper/Scissor - Battle!</h1>
          <ActionButtons />
        </header>

        <section id="instructions">
          { !this.props.applicationStore!.loggedIn && <Instructions /> }
        </section>

        <section id="notifications">
          { this.props.applicationStore!.loggedIn && <Notifications /> }
        </section>

        <section id="game-app">
          { this.props.applicationStore!.loggedIn && <GameApp /> }
        </section>
        
        <footer>
          <a href="https://github.com/daniel-bytes/rps-scala" target="_blank" rel="noopener noreferrer">© Daniel Battaglia 2020</a>
        </footer>
      </div>
    )
  }

  private renderLoading() {
    return <div className="container">loading session...</div>
  }

  private renderError() {
    return (
      <ErrorBox 
        onClearError={ () => this.props.applicationStore!.initializeSessionStoreAsync() } />
    )
  }
}
