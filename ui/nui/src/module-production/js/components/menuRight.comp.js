// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import LetsChatPreview from '../../assets/images/right-menu/app-previews/lets-chat-preview.jpg';
import BucketsPreview from '../../assets/images/right-menu/app-previews/buckets-preview.png';
import WordPressPreview from '../../assets/images/right-menu/app-previews/wordpress-preview.png';
import HackathonPreview from '../../assets/images/right-menu/app-previews/hackathon-preview.png';

let RightMenu = React.createClass({
    
    setPreviewImg: function(){
        
        if(this.props.selectedApp != undefined){        
            switch(this.props.selectedApp.name){
                    case "lets-chat":
                        return LetsChatPreview
                        break;
                    case "buckets":
                        return BucketsPreview
                        break;
                    case "wordpress":
                        return WordPressPreview
                        break;
                    case "hackathon":
                        return HackathonPreview
                        break;
            }
        }
    },
    
    menuClassName: function(){
        
        var isRightMenuShown = this.props.rightMenu.isRender;
        var className;
        
        if(isRightMenuShown){
            className = 'show';
            $("#menu-right").slideDown();
        }else{
            className='hide';
            $("#menu-right").slideUp(200);
        }
        return className;
        
    },
    
   render: function(){    
       //console.log(this.props)
       return (
           
           this.props.rightMenu.isRender ?
           
            <div id="menu-right" className={this.menuClassName()}>
                <div className="title">app preview </div>
                <img src={this.setPreviewImg()}/>
            </div>
           
           :
           
           null
       )
   } 
});

export default RightMenu;