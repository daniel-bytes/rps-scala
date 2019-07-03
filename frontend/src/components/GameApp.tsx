import React, { Component, ReactNode } from 'react'
import { observer, inject } from 'mobx-react'
import GameBoard from './GameBoard'
import GameList from './GameList'
import ErrorBox from './ErrorBox'
import { ApplicationStore } from '../services/ApplicationStore'

interface Props {
  applicationStore?: ApplicationStore
}

@inject("applicationStore")
@observer
export default class GameApp extends Component<Props> {
  async componentDidMount() {
    if (!this.props.applicationStore) throw new Error("missing applicationStore")

    await this.props.applicationStore.initializeGameAppAsync()
  }

  render(): ReactNode {
    return <div>
      <ErrorBox />
      { this.mainRender() }
    </div>
  }

  mainRender(): ReactNode {
    if (this.props.applicationStore!.isLoading) {
      return ( <div>loading... </div> )
    } else {
      if (this.props.applicationStore!.gameInProgress) {
        return (
          <section className="section">
            <GameBoard />
          </section>
        )
      } else {
        return (
          <section className="section">
            <GameList />
          </section>
        )
      }
    }
  }
}
