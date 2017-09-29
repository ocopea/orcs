import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import styles from './styles-prod-header.scss';
import LogoImg from './emc-logo.png';
import UserHandler from '../../../models/User/user-handler';
import { Avatar, BreadCrumbs } from '../../../components';
import Locations from '../../../locations.json';
import { hashHistory } from 'react-router';
import $ from 'jquery';


@inject(["stores"])
export default class ProdHeader extends React.Component{

  constructor(props){
    super(props)
  }

  render(){

    const { t, stores } = this.props;
    const loggedInUser = stores.ui.loggedInUser;
    const avatarUrl = UserHandler.getUserAvatarURL(loggedInUser.id);

    return(
      <header className={styles.Header}>
        <span
          onClick={this.onHamburgerClick.bind(this)}
          id="link" className="icon-hamburger"></span>
        <div id="link" className={styles.logoContainer} onClick={this.onNavigateHome.bind(this)}>
          <img src={LogoImg} className={styles.emcLogo} />
        </div>
        <div className={styles.breadCrumbs}>
          <BreadCrumbs
            currentLocation={stores.ui.currentLocation.pathname} />
        </div>
        <div className={styles.rightSection}>
          <div className={styles.loggedInUser}>
            <Avatar id={styles.avatar} src={avatarUrl} style={{width: 35, height: 35}}/>
            <span className={styles.userName}>{loggedInUser.fullName}</span>
          </div>
          <div className={styles.icons}>
            <span id="link" className="icon-notifications"></span>
            <span id="link" className="icon-settings" onClick={this.onToggleSettingsMenu.bind(this)}></span>
            <span id="link" className="icon-logout"></span>
          </div>

        </div>
      </header>
    )
  }

  onNavigateHome() {
    const currentLocation = this.props.stores.ui.currentLocation.pathname;
    const homeLocation = Locations.production.home.pathname;
    if(currentLocation !== homeLocation) {
      hashHistory.push(Locations.production.home.pathname);
    }
  }

  onHamburgerClick() {
    // this.props.stores.ui.toggleMainMenu();
  }

  onToggleSettingsMenu(e) {
    let position = $(e.target).position();
    position.top += 40;
    position.left -= 85;
    this.props.onToggleSettingsMenu(e, position)
  }

}
