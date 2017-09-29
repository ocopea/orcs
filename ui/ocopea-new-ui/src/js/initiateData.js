// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import UserHandler from './models/User/user-handler';
import AppInstanceHandler from './models/AppInstance/appInstance-handler';
import AppTemplateHandler from './models/AppTemplate/appTemplate-handler';
import QuotaHandler from './models/Quota/quota-handler';
import SiteHandler from './models/Site/site-handler';
import SavedImageHandler from './models/SavedImage/savedImage-handler';


const initiate = () => {
  AppTemplateHandler.fetchAppTemplates();  
  UserHandler.fetchUsers();
  AppInstanceHandler.fetchAppInstance();
  AppInstanceHandler.fetchTestDevAppInstances();
  UserHandler.fetchLoggedInUser();
  QuotaHandler.fetchQuotas();
  SiteHandler.fetchSites();
  SavedImageHandler.fetchSavedImages();
}

export default initiate;
