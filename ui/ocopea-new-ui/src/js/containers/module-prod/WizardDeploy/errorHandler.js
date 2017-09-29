// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
export default class WizardDeployError{

  types;
  typesDictionary;
  error;

  constructor(errorType) {
    this.types = {
      missingAppTemplate: 'missingAppTemplate',
      missingSiteSetup: 'missingSiteSetup',
      missingBusinessPlan: 'missingBusinessPlan',
      missingInstanceName: 'missingInstanceName',
      dsbError: 'dsbError',
      noVersions: 'noVersions'
    };
    this.typesDictionary = {
      missingAppTemplate: "please select app template",
      missingSiteSetup: "please select site setup",
      missingBusinessPlan: "please select business plan",
      missingInstanceName: "please fill in instance name",
      dsbError: "missing dsb",
      noVersions: "no versions found",
    }
    this.error = this.typesDictionary[errorType];
  }

}
