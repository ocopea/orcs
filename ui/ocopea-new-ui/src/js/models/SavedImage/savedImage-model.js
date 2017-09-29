// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
export default class SavedImage {
  id;
  appTemplateId;
  name;
  baseImageId;
  creationTime;
  description;
  createdByUserId;
  state;
  tags;

  constructor(savedImage) {
    this.id = savedImage.id;
    this.appTemplateId = savedImage.appTemplateId;
    this.name = savedImage.name;
    this.baseImageId = savedImage.baseImageId;
    this.creationTime = savedImage.creationTime;
    this.description = savedImage.description;
    this.createdByUserId = savedImage.createdByUserId;
    this.state = savedImage.state;
    this.tags = savedImage.tags;
  }
}
