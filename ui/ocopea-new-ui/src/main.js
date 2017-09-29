// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import ReactDOM from 'react-dom';
import { Router, browserHistory, hashHistory } from 'react-router';
import { I18nextProvider } from 'react-i18next';
import i18n from './js/i18n';
import routes from './js/routes';
import { Provider } from 'mobx-react';
import dataStore from './js/stores/data-store.js';
import uiStore from './js/stores/ui-store.js';
import styles from './scss/general.scss';
import _ from 'lodash';
import Locations from './js/locations.json';
import init from './js/initiateData.js';
import Helper from './js/utils/helper.js';
require('./js/setBodyClassByDevice.js');
require('./js/config.js');
require('style!react-select/dist/react-select.css');

init();

// update ui store with current location
// path from react router
const loc = hashHistory.getCurrentLocation().pathname;
const currentLocation = Helper.getLocationByPathname(loc);
uiStore.setLocation(currentLocation);

hashHistory.listenBefore( location => {
  const pathname = location.pathname;
  const loc = Helper.getLocationByPathname(pathname);
  uiStore.setLocation(loc);
});

const stores = {
  data: dataStore,
  ui: uiStore
}

ReactDOM.render(
      <I18nextProvider i18n={ i18n }>
        <Provider stores={stores}>
          <Router routes={routes} history={hashHistory}/>
        </Provider>
      </I18nextProvider>
  ,
  document.getElementById('root')
);
