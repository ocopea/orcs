// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import PencilIcon from '../../../../assets/images/appTopology/pencil-icon.png';
import SaveIcon from '../../../../assets/images/businessSetup/plan-img-tier2.png';
import Actions from '../../../actions/actions.js';

var EditName = React.createClass({
    
    editAppInstanceName: function(){
        Actions.editAppInstanceName(this.props.editAppInstanceName.appName);
    },
    
    setAppInstanceName: function(e){
        
        var name = e.target.value == "" ? 
                   this.props.editAppInstanceName.appName 
                   : e.target.value;
        
        Actions.setAppInstanceName.bind(this, name)();            
    },
    
    saveAppInstanceName:function(){
        Actions.hideEditAppInstanceName();
    },    
    
    render: function(){
        
        return (
            <div>
            {
                !this.props.editAppInstanceName.isRender ? 
                        <h1 className="general-title">Configure {this.props.editAppInstanceName.appName} 
                            <div className="edit-btn" 
                                onClick={this.editAppInstanceName}>

                                <img src={PencilIcon} id="pencil-icon"/>
                                <span>edit</span>
                            </div>
                        </h1>

                        :

                        <h1 className="general-title">Configure 
                            <input type="text" 
                                defaultValue={this.props.editAppInstanceName.appName} 
                                onChange={this.setAppInstanceName} 
                                id="input-edit-app-instance-name"/>

                            <div 
                                className="edit-btn" 								 
                                onClick={this.saveAppInstanceName}>

                                <img src={SaveIcon} id="pencil-icon"/>
                                <span>save</span>
                            </div>
                        </h1>
            }
            </div>

        )        
    }
    
});

export default EditName;