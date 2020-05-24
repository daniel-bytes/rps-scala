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
      return 'Game state was out of date, try again'
    default: 
      return 'An error occured'
  }
}

const ErrorBox: React.FunctionComponent<Props> = inject("applicationStore")(observer((props) => {
  return ( 
    props.applicationStore!.apiError ?
      <div>
        <button className="delete" onClick={ e => props.applicationStore!.clearError() }>x </button>
        { errorMessageFromCode(props.applicationStore!.apiError) }
      </div> : <span></span>
    )
}))

export default ErrorBox