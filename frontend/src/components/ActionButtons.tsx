import React, { Component } from 'react'
import { observer, inject } from 'mobx-react'
import { NavigationState } from '../models/NavigationModels'
import { IApplicationStore } from '../services/ApplicationStore'


interface Props {
  applicationStore?: IApplicationStore
}

export const ActionButtons: React.SFC<Props> = inject("applicationStore")(observer((props) => {
  if (!props.applicationStore) throw new Error("Missing applicationStore")

  const loginStyle = props.applicationStore.navState === NavigationState.LoginPage ? 'block' : 'none'
  const logoutStyle = props.applicationStore.navState !== NavigationState.LoginPage ? 'block' : 'none'
  const endGameStyle = props.applicationStore.navState === NavigationState.PlayGamePage ? 'block' : 'none'

  return (
    <div className="buttons has-addons is-right">
      <button 
        className="button"
        id="end_game_button" 
        style={{ display: endGameStyle }}
        onClick={ props.applicationStore.endGameButtonPressedAsync }>Start New Game</button>
    
      <button 
        className="button"
        id="authorize_button" 
        style={{ display: loginStyle }}
        onClick={ props.applicationStore.signInButtonPressedAsync }>Sign In With Your Google Account</button>
    
      <button 
        className="button"
        id="signout_button" 
        style={{ display: logoutStyle }}
        onClick={ props.applicationStore.signOutButtonPressedAsync }>Sign Out</button>
    </div> 
  )
}))