// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions/actions.js';

import AppPreviewBtn from '../../assets/images/header/eye-icon.png';
import AppPreviewBtnNotActive from '../../assets/images/header/eye-icon-notActive.png';

let RightMenuBtn = React.createClass({

    handleAppPreviewClick: function(){
        Actions.userClickAppPreview.bind(this, this.props.dashboardAppInstance.webEntryPointURL)();
    },
    
    getClassName:function(){
        var className;
        if(this.props.selectedApp === undefined){
            className='hide';
        }else{
            className='button';
        }
        return className;
    },
    
    getImg: function(){
        var img;
        if(this.props.isRightMenu){
            img = AppPreviewBtn;
        }else{
            img = AppPreviewBtnNotActive;
        }
        return img;
    },
    
    render: function(){
        //console.log(this.props.dashboardAppInstance)
        return (
            
          this.props.navigation == 'dashboard' ?
            
          <div 
                id="app-preview-btn" 
                className={this.getClassName()} 
                onClick={this.handleAppPreviewClick}
                title={this.props.dashboardAppInstance.webEntryPointURL}>
            <div className="inside">
                <img src={this.getImg()} id="eye-icon"/>
            </div>
          </div>  
            
          :
            
          null
        )
    }    
});

export default RightMenuBtn;