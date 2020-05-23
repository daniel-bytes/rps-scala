import * as React from 'react'
import { observer, inject } from 'mobx-react'
import { IApplicationStore } from '../services/ApplicationStore'
import ErrorBox from './ErrorBox'

interface Props {
  applicationStore?: IApplicationStore
}

const Notifications: React.FunctionComponent<Props> = inject("applicationStore")(observer((props) => {
  const combat = props.applicationStore!.combat.map((c: string) => <div>{ c }</div>)

  return ( 
    <div className="subtitle">
      { combat }
      <div>
        { props.applicationStore!.subtitle }
      </div>
      <ErrorBox />
    </div>
  )
}))

export default Notifications