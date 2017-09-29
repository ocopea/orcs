// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Actions from '../actions/actions.js';
import $ from 'jquery';

let RestorePopup = React.createClass({
	
	userClickOnRestoreFromPopup: function(){
		Actions.userClickOnRestoreFromPopup();
	},
	
	userChangedAppNameinPopup: function(e){
        if(e.target.value != undefined){
            Actions.setAppInstanceNameFromDialog(e.target.value);         
            this.getShortAppInstanceId(e.target.value);
        }
		
	},
    
    shortAppInstanceName: "",
    
    getShortAppInstanceId: function(value){
        
        var shortName = "";
        
        if(value.length > 7){
            this.shortAppInstanceName = value.substring(0, 7) + "...";
        }else if(this.shortAppInstanceName != ""){
            this.shortAppInstanceName = value;
        }
    },
    
    componentDidUpdate: function(){
        
        if(this.shortAppInstanceName == ""){
            this.shortAppInstanceName = this.props.data.defaultAppName;    
        }
        
    },
    
    closePopup: function(){
        Actions.userClickOnCloseRetorePopup();
    },
	
    detectEscPressed: function(){
        $(document).on('keyup',function(evt) {
            if (evt.keyCode == 27) {
                Actions.userClickOnCloseRetorePopup();
            }
        }); 
    },
    
	render: function(){
        
        this.props.data.isRender ? this.detectEscPressed() : null
        
		return (
			
			this.props.data.isRender ?
            
			<div id="restore-popup">
			
				<div className="title">
			
                    <div id="content">
                        <span>restore application</span> 
                    </div>
            
                    <button 
                        id="close-restore-popup" 
                        type="button"
                        onClick={this.closePopup}>
			
					  <span></span>
					  <span></span>
			
					</button>
				</div>
                            	
				<div className="inside">

					<table>
						<tbody>
							<tr>
								<td>
									<label>Purpose</label>
								</td>

								<td>
									<select id="purpose-select">
										<option>{this.props.data.purpose[0]}</option>
										<option>{this.props.data.purpose[1]}</option>
										<option>{this.props.data.purpose[2]}</option>
									</select>
								</td>
							</tr>

							<tr>
								<td>
									<label>App name</label>
								</td>
								<td>
									<input 
										type="text" 
										defaultValue={this.props.data.defaultAppName} 
										onChange={this.userChangedAppNameinPopup}/>
								</td>
							</tr>

							<tr>
								<td>
									<label>Site</label>
								</td>

								<td>
									<select id="site-select">
										<option>Dev Space on Cloud</option>
										<option>Local Dev Machine</option>
										<option>Durham ESX2</option>
									</select>
								</td>
							</tr>

							<tr id="btn-container">
								<td colSpan="2">
									<button 
										type="button" 
										id="btn-restore-popup" 
										className="button-primary"
										onClick={this.userClickOnRestoreFromPopup}>restore</button>
								</td>
							</tr>
						</tbody>
			
					</table>
			
				</div>
														 
			</div>
			
			:
			
			null
		)
	}
})

export default RestorePopup;
