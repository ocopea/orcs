// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-filters-list.scss';
import AppInstanceHandler from '../../models/AppInstance/appInstance-handler';
import {CollapsableList} from '../';


@inject(["stores"])
@observer
export default class FiltersList extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t } = this.props;
    const filters = AppInstanceHandler.filters;
    const selectedFilters = AppInstanceHandler.selectedFilters.length ?
                              AppInstanceHandler.selectedFilters : [];

    return(
      <div className={styles.FiltersList}>
        <CollapsableList
          list={filters}
          itemClassName={'Instances-filter__item'}
          selectedItemClassName={'Instances-filter__item--selected'}
          className={'Instances-filter'}
          filters={selectedFilters}
          onItemClick={this.onFilterClick}/>
      </div>
    )
  }

  onFilterClick(item, filter) {
    AppInstanceHandler.onFilterClick(item, filter);
  }

}
