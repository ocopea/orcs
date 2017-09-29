// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/**
 * Add responsive identification class to body element
 * Use function setDocumentByDevice() to initiate
 * Create by Gal Ben Haim 
 */

var mobileMaxWidth = 600;
var tabletMaxWidth = 1280;

setDocumentByDevice()

window.onresize = function(){
    setDocumentByDevice()
};


//Get screen width and height
function getScreenSize(){

    var width = window.innerWidth;
    var height = window.innerHeight;

    return {width: width, height: height};            
}

//Get device name (desktop/tablet/mobile) by width 
function getDeviceByWidth(width){

    var device;

    if(width > tabletMaxWidth){
        device = 'desktop';
    }
    else if(width <= mobileMaxWidth){
        device = 'mobile';
    }
    else if(width > mobileMaxWidth || width <= tabletMaxWidth){
        device = 'tablet';
    }

    return device;
}

//Set body class by device
function setBodyClass(device){
    var body = document.getElementsByTagName("BODY")[0];
    body.className = device;
}

//Listen to screen width and change body 
//class accordingly (desktop/tablet/mobile)
function setDocumentByDevice(){
    var SCREEN_WIDTH = getScreenSize().width;
    var device = getDeviceByWidth(SCREEN_WIDTH);
    setBodyClass(device);
}