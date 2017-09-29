// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import i18n from 'i18next';
import XHR from 'i18next-xhr-backend';
import LanguageDetector from 'i18next-browser-languagedetector';
var resBundle = require(
  "i18next-resource-store-loader!../../locales/index.js"
);

i18n
  .use(XHR)
  .use(LanguageDetector)
  .init({
    resources: resBundle,
    whiteList: ['en-US', 'he'],
    fallbackLng: 'en-US',
    // have a common namespace used around the full app
    ns: ['common'],
    defaultNS: 'common',
    debug: false,
    interpolation: {
      escapeValue: false // not needed for react!!
    },
    backend: {
      // path where resources get loaded from
      loadPath: 'locales/{{lng}}/{{ns}}.json',

      // jsonIndent to use when storing json files
      jsonIndent: 2
    }
  });

export default i18n;
