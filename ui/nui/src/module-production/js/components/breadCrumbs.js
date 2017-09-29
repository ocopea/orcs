// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Separator from '../../assets/images/header/seperator.png';


let BreadCrumbs = React.createClass({
    
    getBreadCrumbs:function(){
//		console.log(this.props)
		var breadcrumb = [];
        var project = "Project Nazgul";
		if(this.props.navigation == 'wizard'){
			var currentStep = this.props.currentStep.name;
			if(this.props.selectedApp != undefined)
				var selectedAppName = this.props.selectedApp.name;
			

			breadcrumb[0] = project;

			if(currentStep != "App Market")
				breadcrumb[1] = selectedAppName;

		}else if(this.props.navigation == 'dashboard'){
			breadcrumb[0] = project;
			if(this.props.selectedAppInstance.appTemplateName != undefined){
				breadcrumb[1] = this.props.selectedAppInstance.appTemplateName;
			}
		}
		
		var crumbs = breadcrumb.map(function(crumb, index){
			if(index != breadcrumb.length-1){
				return  <li key={index}>
							<a>{crumb}</a>
							<span className="separator"> <img src={Separator} className="separator" /> </span>
						</li>
			}else{
				return <li key={index}><a>{crumb}</a></li>    
			}

		});
			
        return crumbs;
    },
    
   render: function() {
//       console.log(this.props)   
       return (
            <div>
               <ul className="breadcrumb">
                {this.getBreadCrumbs()}
               </ul>                      
            </div>
       )
       
   } 
    
});

export default BreadCrumbs; 
     
