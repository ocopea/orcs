// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
export class Site {
  id;
  name;
  urn;
  location;
  spaces;

  constructor(site) {
    this.id = site.id;
    this.name = site.name;
    this.urn = site.urn;
    this.spaces = site.spaces;
    const location = site.location;
    this.location = new Location(location);
  }
}

class Location {
  type;
  geometry;
  properties;

  constructor(location) {
    this.type = location.type;
    const geo = location.geometry;
    const props = location.properties;
    this.geometry = new Geometry(geo);
    this.properties = new Properties(props);
  }
}

class Geometry {
  type;
  coordinates;

  constructor(geometry) {
    this.type = geometry.type;
    this.coordinates = geometry.coordinates;
  }
}

class Properties {
  name;
  siteId;

  constructor(properties) {
    this.name = properties.name;
    this.siteId = properties.siteId;
  }
}
