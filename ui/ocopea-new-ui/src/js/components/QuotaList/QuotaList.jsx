// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-quota-list.scss';
import {ProgressBar} from '../';
import CountTo from 'react-count-to';


@inject(["stores"])
export default class QuotaList extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, list } = this.props;
    const dsbQuota = list.slice(3);

    return(
      <div className={styles.QuotaList}>
        {
          dsbQuota.map((dsbQuota, i) => {
            return <div key={i}>
              <div className={styles.row}>
                <div className={styles.img}>
                  <div>
                    {this.props.img || <span className="icon-postgres"></span>}
                  </div>
                </div>
                <div className={styles.name}>{dsbQuota.name}</div>
                <div className={styles.progress}>
                  <ProgressBar
                    instanceName={dsbQuota.name}
                    progressBarFill={"#7ed396"}
                    width={200}
                    precent={dsbQuota.value}
                    hidePrecent={true}/>
                </div>
                <div className={styles.precent}>
                  <CountTo from={0} to={dsbQuota.value} speed={800}/>%
                </div>
              </div>
            </div>
          })
        }
      </div>
    )
  }

}
