// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
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

export default class Location {
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
