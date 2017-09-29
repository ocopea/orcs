// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import Location from './site-location-model';

export class CopyProtocol {
  protocolName;
  version;

  constructor(copyProtocol) {
    this.protocolName = copyProtocol.protocolName;
    this.version = copyProtocol.version;
  }
}

export class CopyRepo {
  name;
  copyProtocols = [];

  constructor(copyRepo) {
    this.name = copyRepo.name;
    this.copyProtocols = copyRepo.copyProtocols;
  }
}

export class Crb {
  urn;
  type;
  name;
  version;
  copyRepositories = [];

  constructor(crb) {
    this.urn = crb.urn;
    this.type = crb.type;
    this.name = crb.name;
    this.version = crb.version;
    this.copyRepositories = crb.copyRepositories ? crb.copyRepositories.map(cr => new CopyRepo(cr)) : [];
  }
}

class Protocol {
  protocolName;
  version;

  constructor(protocol) {
    this.protocolName = protocol.protocolName;
    this.version = protocol.version;
  }
}

class PlanModel {
  name;
  description;
  cost;
  protocols = [];

  constructor(plan) {
    this.name = plan.name;
    this.description = plan.description;
    this.cost = plan.cost;
    this.protocols = plan.protocols.map(protocol => new Protocol(protocol));
  }
}

export class Dsb {
  urn;
  name;
  description;
  img;
  plans = [];

  constructor(dsb) {
    this.urn = dsb.urn;
    this.name = dsb.name;
    this.description = dsb.description;
    this.img = dsb.img;
    this.plans = dsb.plans.map(plan => new PlanModel(plan));
  }
}

class ArtifactRegistry {
  name;
  type;
  parameters = {};

  constructor(artifactRegistry) {
    this.name = artifactRegistry.name;
    this.type = artifactRegistry.type;
    this.parameters = artifactRegistry.parameters;
  }
}

export class SiteConfig {
  id;
  name;
  urn;
  dsbs;
  crbs;
  artifactRegistries;

  constructor(site) {
    this.id = site.id;
    this.name = site.name;
    this.urn = site.urn;
    this.location = new Location(site.location);
    this.dsbs = site.dsbs.map(dsb => new Dsb(dsb));
    this.crbs = site.copyRepos.map(crb => new Crb(crb));
    this.artifactRegistries = site.artifactRegistries.map(ar => new ArtifactRegistry(ar));
  }
}
