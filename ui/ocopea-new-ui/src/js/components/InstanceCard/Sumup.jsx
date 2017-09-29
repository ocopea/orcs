import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-instance-card.scss';
import { ProgressBarRange, ProgressBar } from '../';


@inject(["stores"])
export default class Sumup extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, numberOfAppServices, numberOfInfraServices, quota } = this.props;

    return(
      <div className={styles.Sumup}>
        <div className={styles.numberOfServices}>
          <span>{numberOfAppServices} </span>
          <span>apps</span>
          <ProgressBar
            precent={78/*numberOfAppServices*/}
            progressBarFill={"#8cc9ea"}/>
        </div>
        <div className={styles.numberOfInfraServices}>
          <span>{numberOfInfraServices} </span>
          <span>services</span>
          <ProgressBarRange dsbQuota={quota.dsbQuota}/>
        </div>
      </div>
    )
  }

}
