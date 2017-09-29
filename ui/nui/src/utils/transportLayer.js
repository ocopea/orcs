// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import $ from 'jquery';

const request = (options, success, error) => {
  $.ajax({
    url: options.url,
    data: JSON.stringify(options.data),
    success: success,
    error: error
  })
}
