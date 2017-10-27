// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import {Quota} from '../';
import styles from './styles-quotas.scss';


@inject(["stores"])
export default class Quotas extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, quota } = this.props;
    const dsbQuotas = quota ? quota.dsbQuota : [];
    const psbQuota = quota ? quota.psbQuota : 0;
    const MAX_QUOTAS = 3;

    return(
      <div className={styles.Quotas}>
        {/* App quota */}
        <Quota
          icon={<span className="icon-app-quota"></span>}
          iconFill={"#8cc9ea"}
          progressBarFill={"#8cc9ea"}
          name={'APP QUOTA'}
          precent={psbQuota} />
        {/* Dsb quotas */}
        {
          dsbQuotas ?
            dsbQuotas.map((dsbQuota, i)=>{
              // !!!! MOCK !!!! //
              const icon = i === 0 ?
                <span className="icon-postgres"></span> :
                <span className="icon-mongo"></span>;
              // !!!! MOCK !!!! //
              if(i < MAX_QUOTAS) {
                return <Quota
                  key={i}
                  style={i===MAX_QUOTAS-1||i===dsbQuotas.length-1?{borderRight: 'none'}:null}
                  icon={icon}
                  iconFill={"#7ed396"}
                  name={dsbQuota.name}
                  precent={dsbQuota.value}
                  progressBarFill={"#7ed396"} />
              }
            })
          : null
        }
        {
          dsbQuotas.length > MAX_QUOTAS ?
            this.getBtnShowMore(dsbQuotas)
          : null
        }
      </div>
    )
  }

  getBtnShowMore(dsbQuotas){
    const uiStore = this.props.stores.ui;
    return (
      <div className={styles.btnShowQuotaList}>
        {/* btn show quota list */}
        <div
          id={styles.btn}
          onClick={()=>{
            uiStore.showLightBox(
              true, uiStore.dialogTypes.quotaList
            )
          }}
          className="link">
            <span className="icon-arrow-right"></span>
        </div>
      </div>
    )
  }

}
