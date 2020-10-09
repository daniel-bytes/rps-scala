import * as React from 'react'
import { observer, inject } from 'mobx-react'
import { IApplicationStore } from '../services/ApplicationStore'

interface Props {
  applicationStore?: IApplicationStore
  onClearError?: () => void
}

const ErrorBox: React.FunctionComponent<Props> = inject("applicationStore")(observer((props) => {
  return ( 
    props.applicationStore!.apiError ?
      <div>
        <button id="error-button" onClick={ 
          () => { 
            props.applicationStore!.clearError()
            if (props.onClearError) props.onClearError()
          }
        }>x</button>
        <em className="error">{ props.applicationStore!.apiError.message }</em>
      </div> : <span></span>
    )
}))

export default ErrorBox