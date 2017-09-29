// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
const appTemplateConfiguration = {
  configuration: (siteId, appTemplateId) => {
    return `${APISERVER}/hub-web-api/test-dev/site/${siteId}/app-template-configuration/${appTemplateId}`
  }
}

export default appTemplateConfiguration;
