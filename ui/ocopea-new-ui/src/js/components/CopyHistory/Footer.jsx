// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';

import ImgCreateCopy from './assets/save.png';
import ImgRestore from './assets/restore.png';
import AppInstanceHandler from '../../models/AppInstance/appInstance-handler';
// import Actions from '../../actions/actions.js';

export default class Footer extends React.Component {

    handleGlobalRestoreButton(){
        // Actions.userClickOnRePurposeCopy();
    }

    getBackupsCount(){

        if(this.props.copies != undefined && this.props.copies[0] != undefined){

            var successCounter = 0;

            this.props.copies.map(function(copy, index){
                if(copy.status == "created" ||
                   copy.status == "failed"){
                    successCounter++;
                }

            });
        }

        return successCounter;

    }

    getFailedCount(){
        //console.log(this.props.copies)
        if(this.props.copies != undefined && this.props.copies[0] != undefined){

            var failCounter = 0;

            this.props.copies.map(function(copy, index){
                if(copy.status == "fail" ||
                   copy.status == "alert"){
                    failCounter++;
                }

            });
        }
        return failCounter;
    }

  	createCopy(){
      const instanceID = this.props.instanceID;
      AppInstanceHandler.createCopy(instanceID);  		
  	}

    render(){

        return(
            <div id="copy-history-footer">

                <div className="section section-left">
                    <span>
                        <span className="success-backups">
                            {this.getBackupsCount()}
                        </span>
                        <span>backups</span>
                    </span>
                    <span>
                        <span className="failed-backups">{this.getFailedCount()}</span>
                        <span>failed</span>
                    </span>
                </div>

                <div className="section section-right">

                    <div className="container create-copy" onClick={this.createCopy.bind(this)}>
                        <img src={ImgCreateCopy} id="img-create-copy"/>
                        <span>create copy</span>
                    </div>

                    <div className="container restore">
                        <img src={ImgRestore} id="img-restore"
                        onClick={this.handleGlobalRestoreButton} />
                        <span>restore</span>
                    </div>

                </div>

            </div>
        )
    }
};
