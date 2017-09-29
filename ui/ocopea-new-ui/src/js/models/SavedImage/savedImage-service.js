// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import DataStore from '../../stores/data-store';
import SavedImage from './savedImage-model';
import mockSavedImages from './mock/mockSavedImages.json';
import Request from '../../transportLayer';

export default class SavedImageService {
  static fetchSavedImages(url) {
     Request(url, {method: 'GET'}, response => {
       const savedImages = response.map(savedImage => {
         return new SavedImage(savedImage);
       });
       DataStore.receiveSavedImages(savedImages)
     }, error => {
       console.log(error)
     })

    // mock
//    const mockImages = mockSavedImages.map(mockSavedImage => new SavedImage(mockSavedImage));
//    DataStore.receiveSavedImages(mockImages);
  }
}
