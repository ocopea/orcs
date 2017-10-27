// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import styles from './styles-site-setup.scss';
import stylesWizard from '../styles-wizard-deploy-prod.scss';
import setupTypes from './setupTypes.json';


@inject(["stores"])
@observer
export default class SiteSetup extends React.Component{

  constructor(props){
    super(props)
    this.state = {
      selected: {}
    }
  }

  render(){

    const {  } = this.props;

    return(
      <div className={styles.SiteSetup}>
        <div className={stylesWizard.title}>site setup</div>
        <div className={stylesWizard.subtitle}>subtitle</div>
        <div className={styles.cards}>
          {
            setupTypes.map(type => {
              const isSelected = this.state.selected.name === type.name;
              const className = type.name.replace(' ', '-');
              return <div className={`${styles.card} ${styles[className]}`}>
                <div className={
                    !isSelected ? `${styles.inside} card-hover` :
                    `${styles.inside} card-hover card-selected`}
                  onClick={this.onSiteSelection.bind(this, type)}>
                  <div className={styles.cardTitle}>{type.name}</div>
                  <img src={require(`./${type.img}`)} />
                  <div className={styles.points}>
                    {this.getPoints(type.points)}
                  </div>
                </div>
              </div>
            })
          }
        </div>
      </div>
    )
  }

  getPoints(pointsCount) {
    let points = [];
    for(let i = 0; i < pointsCount; i++) {
      const point = <div key={i} className={`${styles.point}`}>
        <div className={`${styles.pointInside}`}></div>
      </div>
      points.push(point);
    }
    return points;
  }

  onSiteSelection(setup) {
    this.setState({
      selected: setup
    })
    this.props.onSetSelectedSiteSetup(setup);
  }

}
