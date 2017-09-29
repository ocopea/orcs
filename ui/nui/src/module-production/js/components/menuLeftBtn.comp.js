// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions/actions.js';
import Store from '../stores/main-store.js';
import Config from '../config.js';
import ProdNavigationOptions from '../data/prodNavigationOptions.js';
import DevNavigationOptions from '../../../module-development/js/data/devNavigationOptions.js';


let Hamburger = React.createClass({

    handleHamburgerClick: function() {
        Actions.userClickOnHamburger();
    },
    crossClass: function(){
        var className;
        if(this.props.isRender){
            className = 'cross';
        }else{
            className = 'hamburger';
        }
        if(this.currentHash().module == DevNavigationOptions.module){
          if(this.currentHash().location == this.wizardLocation().dev ||
             this.currentHash().location == "dashboard")
             className = 'hamburger';
           }
        return className;
    },

    currentHash: function(){
      var hash = Config.getCurrentHash(),
          module = hash.module,
          location = hash.location;

          return {module: module, location: location == undefined ? "" : location};
    },

    wizardLocation: function(){
      var prodWizardLocation = ProdNavigationOptions.wizard.
                               location.substring(1, this.currentHash().location.length+1),
          devWizardLocation = ProdNavigationOptions.wizard.
                              location.substring(1, this.currentHash().location.length+1);

          return {prod: prodWizardLocation, dev: devWizardLocation}
    },

    getClass: function(){

      var className;
      if(module == ProdNavigationOptions.module &&
         this.currentHash().location == this.wizardLocation().prod){
          className='hide';
      }else if(this.currentHash().module == ProdNavigationOptions.module){
          className='button show';
      }else if(this.currentHash().location == this.wizardLocation().dev ||
               this.currentHash().location == "dashboard" ||
               this.currentHash().location == DevNavigationOptions.savedImages.location.substring(1)){
          className='disabled';
      }
      return className;
    },
   render: function(){

       return(
        <div id="menu-btn" className={this.getClass()} onClick={this.handleHamburgerClick}>
           <div id="inside">
               <span id="hamburger-first-line" className={this.crossClass()}></span>
               <span id="hamburger-middle-line" className={this.crossClass()}></span>
               <span id="hamburger-last-line" className={this.crossClass()}></span>
           </div>
        </div>
       )
   }
});

export default Hamburger;
