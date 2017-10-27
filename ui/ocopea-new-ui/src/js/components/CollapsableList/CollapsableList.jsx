// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-collapsable-list.scss';
import _ from 'lodash';


@inject(["stores"])
@observer
export default class CollapsableList extends React.Component{

  constructor(props){
    super(props)
    this.state = {
      selected: [],
      itemsSelected: [],
      list: []
    }
  }

  static propTypes = {
    list: React.PropTypes.object.isRequired, // { liName: {name: '', items: [] }
    checkIcon: React.PropTypes.object, // component
    filters: React.PropTypes.array
  }

  render(){

    const { t, list, filters } = this.props;
    const _filters = filters || [];

    return(
      <ul id={styles.CollapsableList} className={this.props.className}>
        {
          list ?
            _.map(list, li => {
              const liName = li.name;
              const isSelected = this.state.selected.indexOf(li) > -1;
              const iconClassName = li.iconClassName;
              const items = li.items || [];

              return (
                <li key={liName} className={styles.li} onClick={this.onLiClick.bind(this, li)}>
                  <div className={styles.liTop}>
                    {
                      iconClassName ?
                        <span className={li.iconClassName}></span>
                      :
                      null
                    }
                    <span className={liName}>{liName}</span>
                  </div>
                  {/* collapsed section */}
                  <div id={`collapse ${liName}`} className={
                      isSelected ? styles.collapseOpen : styles.collapse}>
                      {
                        items.map(item => {
                          const itemName = item.name;
                          const isItemSelected = _.filter(this.props.filters, f=>{return f.name === item.name})[0];
                          return <div
                                    key={itemName}
                                    id={styles.subLi}
                                    className={
                                      isItemSelected ?
                                      this.props.selectedItemClassName : this.props.itemClassName}
                                    onClick={this.onItemClick.bind(this, item, li)}
                                    id="collapse">
                              <span id="collapse"
                                className={isItemSelected ? styles.itemSelected : styles.item}>
                                <span className="dot"></span>
                              </span>
                            <span id="collapse">{itemName}</span>
                          </div>
                        })
                      }
                  </div>
                  {
                    items.length ?
                      <span
                        id={isSelected ? styles.iconArrowClose : styles.iconArrowOpen}
                        className="icon-arrow-dropdown"></span>
                    : null
                  }
                </li>
              )
            })
          : null
        }
      </ul>
    )
  }

  onItemClick(item, e) {
    this.props.onItemClick ? this.props.onItemClick(item, e) : null;
  }

  onLiClick(li, e) {
    this.props.onLiClick ? this.props.onLiClick(li, e) : null;
    if(e.target.id !== 'collapse') {
      let selected = this.state.selected;
      selected.indexOf(li) === -1 ? selected.push(li) :
        selected.splice(selected.indexOf(li), 1);
      this.setState({
        selected: selected
      });
    }
  }

}
