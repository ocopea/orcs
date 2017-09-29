// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import { Route, IndexRedirect } from 'react-router';
import Locations from './locations.json';
import { Entry,
         PageNotFound404,
         DevHome,
         Dashboard,
         Settings,
         ProdHome,
         WizardDeployProd,
         AppMarket,
         AppTopologyContainer,
         SiteConfig,
         SiteSetup,
         BusinessSetup,
         Configuration,
         SavedImagesList,
         ProdDeployingProgress,
         ProdDashboard} from './containers';
import { hashHistory } from 'react-router';


module.exports = (
    <Route path="/" component={Entry}>

      // initial component - set default here
      <IndexRedirect to={Locations.development.home.pathname} component={DevHome} />

      // DEVELOPMENT MODLE
      <Route path={Locations.development.home.pathname} component={DevHome} />
      <Route
        path={`${Locations.development.dashboard.pathname}/:instanceID`}
        component={Dashboard}/>

      // PRODUCTION MODULE
      <Route path={Locations.production.home.pathname} component={ProdHome}/>

      // deploy wizard
      <Route component={WizardDeployProd} >
        <Route
          path={Locations.production.wizardDeploy.steps.appMarket.pathname}
          component={AppMarket}/>
        <Route
          path={Locations.production.wizardDeploy.steps.appTopology.pathname}
          component={AppTopologyContainer}/>
        <Route
          path={Locations.production.wizardDeploy.steps.siteSetup.pathname}
          component={SiteSetup}/>
        <Route
          path={Locations.production.wizardDeploy.steps.businessSetup.pathname}
          component={BusinessSetup}/>
        <Route
          path={Locations.production.wizardDeploy.steps.configuration.pathname}
          component={Configuration}/>
      </Route>
      // dashboard
      <Route
        path={`${Locations.production.dashboard.pathname}/:instanceID`}
        component={ProdDashboard}/>
      // deploying progress
      <Route
        path={`${Locations.production.deployingProgress.pathname}/:appTemplateID`}
        component={ProdDeployingProgress}/>

      // settings page
      <Route path={Locations.settings.home.pathname} component={Settings}/>

      // site config page
      <Route path={Locations.siteConfig.home.pathname} component={SiteConfig}/>

      <Route path={Locations.development.savedImages.pathname} component={SavedImagesList}/>

      // 404 page not found
      <Route path='*' component={PageNotFound404} />

    </Route>
);
