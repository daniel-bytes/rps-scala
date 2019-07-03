import React from 'react';
import AuthedApp from './components/AuthedApp'

interface Props {}

export const App: React.SFC<Props> = (props) => {
  return (
    <section className="section">
      <AuthedApp />
    </section>
  )
}

export default App;
