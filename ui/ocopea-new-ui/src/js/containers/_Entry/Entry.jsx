import React from 'react';
import { observer, inject } from 'mobx-react'
import styles from './style-entry.scss';
import { Header, LightBox,
         ButtonDismiss, QuotaList,
         MainMenu, MenuSettings, RestoreCopy } from '../../components';
import AppInstanceHandler from '../../models/AppInstance/appInstance-handler';
import Loader from './loader.gif';
import modules from '../../utils/modules.json';

import $ from 'jquery';


@inject(["stores"])
@observer
class Entry extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      settingsMenu: {
        isRender: false
      }
    }
    window.addEventListener('click',this.clickOnDocument.bind(this),true);
  }

  render() {

    const { t, stores } = this.props;

    return (
      <div style={this.containerStyle()}>
        {/* Main Container */}
        <main className={this.getMainClassName()}>
          {/* Header */}
          <Header
            module={stores.ui.module}
            loggedInUser={stores.ui.loggedInUser}
            onToggleSettingsMenu={this.onToggleSettingsMenu.bind(this)}/>
          {/* Inside */}
          <div className={styles.ApplicationInside}>
            {/* Main menu */}
            {
              stores.ui.mainMenu.isRender && !_.isEmpty(stores.ui.currentLocation) ?
                <MainMenu />
              : null
            }
            {/* containers */}
            {this.props.children}
            {/* settings menu */}
            {
              this.state.settingsMenu.isRender ?
                <MenuSettings
                  position={this.state.settingsMenu.position}
                  onDismiss={this.dismissSettingsMenu.bind(this)}/>
              : null
            }
          </div>
        </main>

        {/* loader */}
        {
           this.isLoading() ?
            <img src={Loader} className={styles.loader}/>
          : null
        }

        {/* Light boxs */}
        {
          stores.ui.selectedDialogType === stores.ui.dialogTypes.quotaList ?
            <LightBox
              title='quotas list'
              component={<QuotaList list={stores.data.quota.dsbQuota || []} />}
              width={456}
              height={390}/>
          :
          stores.ui.selectedDialogType === stores.ui.dialogTypes.restoreCopy ?
            <LightBox
              title='restore copy'
              component={
                <RestoreCopy
                  copy={stores.ui.lightBox.props}
                  instance={AppInstanceHandler.getInstanceById(this.props.params.instanceID)} />
              }
              width={370}
              height={355}/>
          : null
        }
      </div>
    );
  }

  clickOnDocument(e) {
    if(this.state.settingsMenu.isRender)
      this.removeSettingsMenu(e);
  }

  removeSettingsMenu(e) {
    const classList = e.target.classList;
    const id = e.target.id;
    const isMenu = id === 'settings-menu' || classList.contains('settings-menu');
    const isBtn = classList.contains('icon-settings');
    let menuChild = false;
    $(e.target).parents().each((index,element)=>{
      if(element.classList.contains('settings-menu')){
        menuChild = true;
        return;
      }
    });
    const shouldToggle = !isMenu && !isBtn && !menuChild;
    shouldToggle ? this.dismissSettingsMenu() : null;
  }

  dismissSettingsMenu() {
    this.setState({settingsMenu:{isRender:false}})
  }

  onToggleSettingsMenu(e, position) {

    const isSettingsBtn = e.target.classList.contains('icon-settings');
    const isSettingsMenu = e.target.id === 'settings-menu';

    this.setState({
      settingsMenu: {
        isRender: !this.state.settingsMenu.isRender,
        position: position
      }
    });
  }

  isLoading() {
    return this.props.stores.ui.pendingRequests.length > 0
  }

  isDimmed() {
    return this.isLoading() || this.props.stores.ui.lightBox.isRender;
  }

  getMainClassName() {
    return !this.isDimmed() ?
              styles.ApplicationWrapper :
              styles.ApplicationWrapperDimmed;
  }

  containerStyle() {
    return this.isDimmed() ?
            {height:'100%', background: '#000',
              display: 'table', transition: '0.4s', margin:'auto'} :
            {height:'100%', transition: '0.4s'};
  }

}

export default Entry;
