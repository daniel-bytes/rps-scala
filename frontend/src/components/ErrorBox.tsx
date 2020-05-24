import * as React from 'react'
import { observer, inject } from 'mobx-react'
import { IApplicationStore } from '../services/ApplicationStore'

interface Props {
  applicationStore?: IApplicationStore
}

function errorMessageFromCode(code: string): string {
  switch(code) {
    case 'not-a-movable-token': 
      return 'Not a movable token'
    case 'version-conflict':
    case 'game-not-found':
      return 'Game was out of sync and had to be refreshed, try again'
    default: 
      return 'An error occured'
  }
}

const ErrorBox: React.FunctionComponent<Props> = inject("applicationStore")(observer((props) => {
  return ( 
    props.applicationStore!.apiError ?
      <div>
        <button id="error-button" onClick={ e => props.applicationStore!.clearError() }>x</button>
        <em className="error">{ errorMessageFromCode(props.applicationStore!.apiError) }</em>
      </div> : <span></span>
    )
}))

export default ErrorBox