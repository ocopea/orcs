// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-main-screen-menu.scss';
import { hashHistory } from 'react-router';
import Locations from '../../../../locations.json';


@inject(["stores"])
@observer
export default class MainScreenMenu extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const {  } = this.props;

    return(
      <div className={styles.MainScreenMenu}>
        <div>
          <button
            onClick={this.onCreateNewClick.bind(this)}
            type="button"
            className={styles.btnGoToWWizard}>
            <span className="icon-create"></span>
            add application
          </button>
        </div>
      </div>
    )
  }

  onCreateNewClick() {
    hashHistory.push(Locations.production.wizardDeploy.steps.appMarket.pathname);
  }

}
