// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-bread-crumbs.scss';
import _ from 'lodash';


@inject(["stores"])
@observer
export default class BreadCrumbs extends React.Component{

  constructor(props){
    super(props)
    this.update();
  }


  render(){

    const { currentLocation } = this.props;
    const crumbs = this.parseCrumbs(this.props.currentLocation);

    return(
      <div className={styles.BreadCrumbs}>
        {
          crumbs.map((crumb, i) => {
            return <div className={styles.breadCrumbs}>
              <span>{crumb}</span>
              {
                i !== crumbs.length - 1 ?
                  <span className={styles.seperator}>></span>
                : null
              }
            </div>
          })
        }
      </div>
    )
  }

  parseCrumbs(locations) {
    const crumbs = _.split(locations, '/')
    crumbs.splice(0, 1);
    return crumbs;
  }

  update() {
    const crumbs = this.parseCrumbs(this.props.currentLocation);
    const currentLocation = this.props.currentLocation;
    this.state = {
      currentLocation: currentLocation,
      crumbs: crumbs
    }
  }


}
