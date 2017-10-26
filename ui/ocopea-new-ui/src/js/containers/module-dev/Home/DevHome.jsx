import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-dev-home.scss';
import Locations from '../../../locations.json';
import mockQuota from '../../../models/Quota/mock/mockQuota';
import { Quota } from '../../../models/Quota/quota-model';
import { Quotas, InstanceCards, InstancesList } from '../../../components';
import CardsControllBar from './CardsControllBar.jsx';
import AppInstanceHandler from '../../../models/AppInstance/appInstance-handler';


@inject(["stores"])
@observer
export default class DevHome extends React.Component{

  render(){

    const { t, stores } = this.props;
    const quota = stores.data.quota;
    const mock_quota = new Quota(mockQuota);
    const psbQuota = quota ? quota.psbQuota : 0;
    const dsbQuotas = quota ? quota.dsbQuota : [];
    const layouts = this.state.layouts;
    const selectedLayout = this.state.selectedLayout;
    const appInstances = AppInstanceHandler.filteredInstances;
    const isMax = stores.ui.mainMenu.isRender;

    return(

      <div id={isMax ? 'minimize' : null} className={styles.Home}>

        {/* top container */}
        <div className={styles.topContainer}>
          <div className={styles.org}>
            <span>org</span>
            <span>Ocopea</span>
          </div>

          <div>
            <Quotas quota={mock_quota}/>
          </div>
        </div>

        <div className={styles.inside}>

          <CardsControllBar
            layouts={layouts}
            selectedLayout={selectedLayout}
            onLayoutSelection={this.onLayoutSelection.bind(this)}/>

          {
            this.state.selectedLayout === this.state.layouts.grid ?
              <InstanceCards list={appInstances}/>
            :
            this.state.selectedLayout === this.state.layouts.list ?
              <div>
                <InstancesList list={appInstances}/>
              </div>
            : null
          }

        </div>

      </div>

    )
  }

  constructor(props){
    super(props)
    this.state = {
      layouts: {
        grid: 'grid',
        list: 'list'
      },
      selectedLayout: 'grid'
    }
  }

  onLayoutSelection(layout) {
    this.setState({
      selectedLayout: layout
    })
  }

}
