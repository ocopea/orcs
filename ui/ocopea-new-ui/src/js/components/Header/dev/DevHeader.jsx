import React from 'react';
import { observer, inject } from 'mobx-react';
import { translate } from 'react-i18next';
import { hashHistory } from 'react-router';
import { Avatar } from '../../';
import styles from './styles-dev-header.scss';
import Locations from '../../../locations';
import modules from '../../../utils/modules';
import UserHandler from '../../../models/User/user-handler';
import UserApi from '../../../models/User/user-api';
import $ from 'jquery';


@inject(["stores"])
@translate(['general'], { wait: true })
export default class Header extends React.Component{

  render(){

    const { t, stores } = this.props;
    const loggedInUser = stores.ui.loggedInUser;
    const loggedInUserFullname = loggedInUser ?
      `${loggedInUser.firstName} ${loggedInUser.lastName}` : null;
    const avatar = loggedInUser ? UserApi.avatar(loggedInUser.id) : null;
    const currentLocationName = stores.ui.currentLocation.name || 'page not found';
    const isMax = false;

    return(
      <header id={isMax ? null : styles.HeaderMinimized} className={styles.Header}>
        <div className={styles.inside}>

          {/* Toggle main menu button */}
          {
            currentLocationName === Locations.development.home.name ?
              <button onClick={this.toggleMainMenu.bind(this)} className={styles.filterIconContainer}>
                <span className="icon-filter"></span>
              </button>
            :
            <span
              onClick={this.onHamburgerClick.bind(this)}
              id="link" className='icon-hamburger'></span>
          }

          {/* Link home */}
          <section
            onClick={this.navigateHome}
            className={styles.linkHome}
            title="home">
              <span>Ocopea</span>
          </section>

          {/* Current location */}
          <section className={styles.currentLocation}>
            {currentLocationName}
          </section>

          {/* icons */}
          <section className={styles.icons}>
            <span id="link" className="icon-backup"></span>
            <span id="link" className="icon-alerts"></span>
            <span id="link" className="icon-settings"
              onClick={this.onToggleSettingsMenu.bind(this)}></span>
            <span id="link" className="icon-link"></span>
          </section>

          {/* user settings */}
          <section className={styles.userSettings}>
            {
              avatar ?
                <div className={styles.avatarContainer}>
                  <Avatar src={avatar} />
                </div>
              : null
            }
            <span
              title={loggedInUserFullname}
              className={styles.username}>
              {loggedInUserFullname}
            </span>
            {this.getToggleButton()}
          </section>

        </div>
      </header>
    )
  }

  constructor(props){
    super(props)
  }

  onHamburgerClick() {
    this.props.stores.ui.toggleMainMenu();
  }

  toggleMainMenu() {
    this.props.stores.ui.toggleMainMenu();
  }

  navigateHome() {
    const currentLocation = hashHistory.getCurrentLocation().pathname;
    if(currentLocation !== Locations.development.home.pathname){
      hashHistory.push(Locations.development.home);
    }
  }

  getToggleButton() {
    return <span
        id="link"
        className="icon-arrow-dropdown"></span>
  }

  onToggleSettingsMenu(e) {
    let position = $(e.target).position();
    position.top += 40;
    position.left -= 85;
    this.props.onToggleSettingsMenu(e, position)
  }

}
