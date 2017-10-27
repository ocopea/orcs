// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-dev-home.scss';
import {Filters} from '../../../components';
import AppInstanceHandler from '../../../models/AppInstance/appInstance-handler';


@inject(["stores"])
@observer
export default class CardsControllBar extends React.Component{

  render(){

    const { t } = this.props;
    const selectedLayout = 'grid';
    const filters = AppInstanceHandler.selectedFilters;

    const filterStyle = {
      background:"#f0f4fa",
      lineHeight: "30px",
      paddingLeft: "10px",
      height: '32px',
      width: '98px',
      borderRadius: '3px'
    }

    return(
      <div className={styles.cardsControllBar}>
        <div className={styles.filters}>
          <Filters
            onFilterClick={this.onFilterClick.bind(this)}
            filterStyle={filterStyle}
            filters={filters}/>
        </div>
        <div className={styles.sort}>
          <span>sort by</span>
          <span className="icon-arrow-dropdown"></span>
        </div>
        <div className={styles.layoutSelection}>
          <span
            onClick={this.onLayoutSelection.bind(this, this.props.layouts.list)}
            style={this.getLayoutBtnStyle(this.props.layouts.list)}
            id="link"
            className="icon-layout-list"></span>
          <span
            onClick={this.onLayoutSelection.bind(this, this.props.layouts.grid)}
            style={this.getLayoutBtnStyle(this.props.layouts.grid)}
            id="link"
            className="icon-layout-cards"></span>
        </div>
      </div>
    )
  }

  constructor(props){
    super(props)
  }

  onLayoutSelection(layout) {
    this.props.onLayoutSelection(layout);
  }

  onFilterClick(filter) {
    AppInstanceHandler.onFilterClick(filter, AppInstanceHandler.filters[filter.type]);
  }

  getLayoutBtnStyle(selectedLayout) {
    if(this.props.selectedLayout === selectedLayout) {
      return {color: "#479dda"}
    }else{
      return {color: "#e1e7f0"}
    }
  }

}
