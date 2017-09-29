// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
var date = new Date();
var mockDate1 = date.setDate(date.getDate() + 1);
var mockDate2 = date.setDate(date.getDate() + 2);
var mockDate3 = date.setDate(date.getDate() + 3);
var mockDate4 = date.setDate(date.getDate() + 4);

var ImageBackups = [
  { timestamp: mockDate1 ,size: 1.63, location: 'cloud' },
  { timestamp: mockDate1 ,size: 1.63, location: 'cloud-some' },
  { timestamp: mockDate2 ,size: 5.07, location: 'cloud' },
  { timestamp: mockDate3 ,size: 3.5, location: 'local' },
  { timestamp: mockDate4 ,size: 8.5, location: 'local' }
]

export default ImageBackups;
