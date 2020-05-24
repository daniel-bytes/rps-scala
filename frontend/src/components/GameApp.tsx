import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import GameBoard from './GameBoard'
import { ApplicationStore } from '../services/ApplicationStore'

interface Props {
  applicationStore?: ApplicationStore
}

@inject("applicationStore")
@observer
export default class GameApp extends Component<Props> {
  async componentDidMount() {
    await this.props.applicationStore!.initializeGameAppAsync()
  }

  render() {
    return <div className="game-app"><GameBoard /></div>
  }
}
