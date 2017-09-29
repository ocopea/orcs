import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-filters.scss';
import {ButtonDismiss} from '../';
import AppInstanceHandler from '../../models/AppInstance/appInstance-handler';


export default class Filters extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t } = this.props;

    return(
      <div className={styles.Filters}>
        {
          this.props.filters.map((filter, i) => {
            return (
              <div style={this.props.filterStyle} key={i} className={styles.filter}>
                <span>{filter.name}</span>
                <ButtonDismiss
                  onClick={this.onFilterClick.bind(this, filter)}
                  style={{width:10, height:10}}
                  color={this.getColor(filter.type)} />
              </div>
            )
          })
        }
      </div>
    )
  }

  getColor(type) {
    switch (type) {
      case AppInstanceHandler.filterTypes.appType.key:
        return '#479dda';
        break;
      case AppInstanceHandler.filterTypes.service.key:
        return '#8286de';
        break;
      case AppInstanceHandler.filterTypes.user.key:
        return '#3ac187';
        break;
    }
  }

  onFilterClick(filter) {
    if(this.props.onFilterClick) {
      this.props.onFilterClick(filter);
    }
  }

}
