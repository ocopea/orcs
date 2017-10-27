// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { observer, inject } from 'mobx-react';
import { CollapsableList } from '../';
import SettingsMenuList from './MenuSettingsList.js';
import styles from './styles-menu-settings.scss';
import {hashHistory} from 'react-router';
import Locations from '../../locations.json';
import modules from '../../utils/modules.json';


@inject(["stores"])
@observer
export default class MenuSettings extends React.Component{

  constructor(props){
    super(props)
    this.state = {
      top: props.position.top,
      left: props.position.left
    }
  }

  static propTypes = {
    onDismiss: React.PropTypes.func.isRequired
  }

  render(){

    const { } = this.props;

    return(
      <div
        style={{top: this.state.top, left: this.state.left}}
        id="settings-menu" className={styles.MenuSettings}>
        <CollapsableList
          list={SettingsMenuList}
          className="settings-menu"
          onItemClick={this.onItemClick.bind(this)}
          onLiClick={this.onItemClick.bind(this)} />
      </div>
    )
  }

  componentDidMount() {
    this.setState({
      top: this.props.position.top,
      left: this.props.position.left
    });
  }

  onItemClick(item, e) {
    const pathname = item.pathname;
    if(pathname) {
      hashHistory.push(pathname);
      this.props.onDismiss();
    }
  }

}
