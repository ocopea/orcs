// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import uiStore from './stores/ui-store.js';
import $ from 'jquery';


const request = (url, options, onsuccess, onerror, hideLoader) => {
  
  !hideLoader ? uiStore.addPendingRequest(options.requestName) : null;

  $.ajax({
    headers: {
      'X-CSRFToken': getCookie('csrftoken')
    },
    url: url,
    method: options.method,
    contentType: options.contentType,
    data: JSON.stringify(options.data),
    success: (response)=>{
      onsuccess(response);
      !hideLoader ? uiStore.removePendingRequest(options.requestName) : null;
    },
    error: (error)=>{
      onerror(error);
      !hideLoader ? uiStore.removePendingRequest(options.requestName) : null;
    }
  })

}

function getCookie(name) {
    var cookieValue = null;
    if (document.cookie && document.cookie !== '') {
        var cookies = document.cookie.split(';');
        for (var i = 0; i < cookies.length; i++) {
            var cookie = $.trim(cookies[i]);
            // Does this cookie string begin with the name we want?
            if (cookie.substring(0, name.length + 1) === (name + '=')) {
                cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                break;
            }
        }
    }
    return cookieValue;
}

export default request;
