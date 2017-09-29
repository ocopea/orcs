// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Locations from '../../locations.json';

export default {
  "userSettings": {
    "name": "user settings",
    "items": [],
    "iconClassName": "icon-user-settings"
  },
  "recentActivities": {
    "name": "recent activities",
    "items": [],
    "iconClassName": "icon-activities"
  },
  "summaryReport": {
    "name": "summary report",
    "items": [],
    "iconClassName": "icon-reports"
  },
  "savedImages": {
    "name": "saved images",
    "items": [],
    "iconClassName": "icon-saved-images",
    "pathname": Locations.development.savedImages.pathname
  },
  "settings": {
    "name": "settings",
    "items": [],
    "iconClassName": "icon-settings",
    "pathname": Locations.settings.home.pathname
  },
  "siteConfig": {
    "name": "site config",
    "items": [],
    "iconClassName": "icon-site-config",
    "pathname": Locations.siteConfig.home.pathname,
  },
}
