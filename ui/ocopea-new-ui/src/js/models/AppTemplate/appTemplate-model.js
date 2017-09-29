// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
export class AppTemplate {
  id;
  name;
  version;
  description;
  appServiceTemplates;
  entryPointServiceName;
  img;

  constructor(appTemplate) {
    const appServiceTemplates = appTemplate.appServiceTemplates;
    const dependencies = appServiceTemplates.dependencies;

    this.id =                   appTemplate.id;
    this.img =                  appTemplate.img;
    this.name =                 appTemplate.name;
    this.version =              appTemplate.version;
    this.description =          appTemplate.description;
    this.appServiceTemplates =  this.getAppServiceTemplate(appServiceTemplates);
  }

  getAppServiceTemplate(appServiceTemplates) {
    return appServiceTemplates.map(appServiceTemplate => {
      return new AppServiceTemplate(appServiceTemplate);
    });
  }

}


class AppServiceTemplate {
  appServiceName;
  psbType;
  imageName;
  imageType;
  imageVersion;
  environmentVariables;
  dependencies;
  exposedPorts;
  httpPort;
  entryPointUrl;
  img;

  constructor(appServiceTemplate) {
    this.appServiceName = appServiceTemplate.appServiceName;
    this.psbType = appServiceTemplate.psbType;
    this.imageName = appServiceTemplate.imageName;
    this.imageType = appServiceTemplate.imageType;
    this.imageVersion = appServiceTemplate.imageVersion;
    this.environmentVariables = appServiceTemplate.environmentVariables;
    this.dependencies = this.getDependencies(appServiceTemplate.dependencies);
    this.exposedPorts = appServiceTemplate.exposedPorts;
    this.httpPort = appServiceTemplate.httpPort;
    this.entryPointUrl = appServiceTemplate.entryPointUrl;
    this.img = appServiceTemplate.img;
  }

  getDependencies(dependencies) {
    return dependencies.map(dependency => {
      return new Dependency(dependency);
    });
  }

}


class Dependency {
  type;
  name;
  description;
  img

  constructor(dependency) {
    this.type = dependency.type;
    this.name = dependency.name;
    this.description = dependency.description;
    this.img = dependency.img;
  }
}
