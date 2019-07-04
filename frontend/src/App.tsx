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
      <section className="section">
        <div className="container is-fluid">
          <div className='columns'>
            <div className='column'>
              <h1 className="title">Rock/Paper/Scissor</h1>
            </div>
            <div className='column'>
              <h3 className="subtitle is-3 center">
                { this.props.applicationStore!.subtitle }
              </h3>
            </div>
            <div className='column'>
              <ActionButtons />
            </div>
          </div>
          <ErrorBox />
          { this.props.applicationStore!.loggedIn && <GameApp /> }
        </div>
      </section>
    )
  }

  private renderLoading() {
    return <div className="container">loading session...</div>
  }
}
