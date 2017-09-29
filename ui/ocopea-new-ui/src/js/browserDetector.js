// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
// import Actions from './actions/actions.js';

var BrowserDetector = {

    getBrowserName: function(){
        // Opera 8.0+
        var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
        // Firefox 1.0+
        var isFirefox = typeof InstallTrigger !== 'undefined';
        // At least Safari 3+: "[object HTMLElementConstructor]"
        var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;
        // Internet Explorer 6-11
        var isIE = /*@cc_on!@*/false || !!document.documentMode;
        //Internet Explorer Edge
        var isExplorerEdge = navigator.userAgent.indexOf('Edge') >= 0;
        // Chrome 1+
        var isChrome = !!window.chrome && !!window.chrome.webstore;

        var result = {
            opera: isOpera,
            firefox: isFirefox,
            safari: isSafari,
            ie: isIE,
            chrome: isChrome,
            edge: isExplorerEdge
        }

        var browser = "";

        for(var key in result){
            if(result[key]){
                browser = key;
            }
        }

        return this.getCleanName(browser);
    },

    getCleanName: function(browserName){
        var cleanNames = {
            chrome: this.browserNames().chrome,
            safari: this.browserNames().safari,
            firefox: this.browserNames().firefox,
            ie: this.browserNames().ie,
            edge: this.browserNames().edge
        }

        if(cleanNames[browserName] == undefined){
            return "browser not detected!";
        }else{
            return cleanNames[browserName];
        }
    },

    redirectIfNotChrome: function(){

        var browserName = BrowserDetector.getBrowserName();

        // if(browserName != "Chrome"){
        //     Actions.browserDetectionRedirect.bind(this, browserName)();
        // }

    },

    blockBrowserByName: function(browsersToBlock){

        var browserName = BrowserDetector.getBrowserName();

        browsersToBlock.forEach(function(browserToBlock){
            // if(browserName == browserToBlock){
            //     Actions.blockBrowserByName.bind(this, browserName)();
            // }
        })
    },

    browserNames: function(){
      return {
        chrome: "Chrome",
        safari: "Safari",
        firefox: "Fire Fox",
        ie: "Internet Explorer",
        edge: "Edge"
      }
    },

    isBrowser: function(browser){
      return BrowserDetector.getBrowserName() === browser;
    }

}

export default BrowserDetector;
