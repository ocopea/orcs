import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-prod-main-menu.scss';
import Locations from '../../../locations.json';
import { hashHistory } from 'react-router';
import MainScreenMenu from './MainScreenMenu/MainScreenMenu.jsx';
import WizardDeployMenu from './WizardDeployMenu/WizardDeployMenu.jsx';
import DashboardMenu from './DashboardMenu/DashboardMenu.jsx';
import Helper from '../../../utils/helper';


@inject(["stores"])
@observer
export default class ProdMainMenu extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { stores } = this.props;
    const currentLocation = stores.ui.currentLocation.pathname;
    const component = this.getComponent(currentLocation);

    return(
      <div className={styles.MainMenu}>
        {component}
      </div>
    )
  }

  getComponent(currentStepPath) {
    const steps = Locations.production.wizardDeploy.steps;
    const wizardPathname = Locations.production.wizardDeploy.pathname;
    const isWizard = currentStepPath.indexOf(wizardPathname) > -1;
    const isHome = currentStepPath === Locations.production.home.pathname;
    const isDashboard = currentStepPath === Locations.production.dashboard.pathname;
    const instances = this.props.stores.data.appInstances.slice();
    
    if(isWizard) {
      return <WizardDeployMenu steps={steps}/>;
    }
    if(isHome) {
      return <MainScreenMenu />;
    }
    if(isDashboard) {
      return <DashboardMenu instances={instances}/>
    }
  }

}
