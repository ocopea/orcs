// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import SavedImageApi from './savedImage-api';
import SavedImageService from './savedImage-service';

export default class SavedImageHandler {

  static fetchSavedImages() {
    SavedImageService.fetchSavedImages(SavedImageApi.savedImage);
  }
}
