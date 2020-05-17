import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import { NavigationState } from '../models/NavigationModels'
import { IApplicationStore } from '../services/ApplicationStore'


interface Props {
  applicationStore?: IApplicationStore
}

export const ActionButtons: React.SFC<Props> = inject("applicationStore")(observer((props) => {
  if (!props.applicationStore) throw new Error("Missing applicationStore")

  const loginStyle = props.applicationStore.navState === NavigationState.LoginPage ? 'inline' : 'none'
  const logoutStyle = props.applicationStore.navState !== NavigationState.LoginPage ? 'inline' : 'none'
  const endGameStyle = props.applicationStore.navState === NavigationState.PlayGamePage ? 'inline' : 'none'

  return (
    <div className="buttons">
      <button 
        className="button"
        id="end_game_button" 
        style={{ display: endGameStyle }}
        onClick={ props.applicationStore.endGameButtonPressedAsync }>New Game</button>
    
      <button 
        className="button"
        id="authorize_button" 
        style={{ display: loginStyle }}
        onClick={ props.applicationStore.signInButtonPressedAsync }>Sign In With Google To Play</button>
    
      <button 
        className="button"
        id="signout_button" 
        style={{ display: logoutStyle }}
        onClick={ props.applicationStore.signOutButtonPressedAsync }>Sign Out</button>
    </div> 
  )
}))