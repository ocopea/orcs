// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';

import ImgCreateCopy from '../../../assets/images/dashboard/copyHistory/footer/save.png';
import ImgRestore from '../../../assets/images/dashboard/copyHistory/footer/restore.png';
import Actions from '../../actions/actions.js';

let CopyHistoryFooter = React.createClass({

    handleGlobalRestoreButton: function(){
        Actions.userClickOnRePurposeCopy();
    },

    getBackupsCount: function(){

        if(this.props.allCopies != undefined && this.props.allCopies[0] != undefined){

            var successCounter = 0;

            this.props.allCopies.map(function(copy, index){
                if(copy.status == "created" ||
                   copy.status == "failed"){
                    successCounter++;
                }

            });
        }

        return successCounter;

    },

    getFailedCount: function(){
        //console.log(this.props.allCopies)
        if(this.props.allCopies != undefined && this.props.allCopies[0] != undefined){

            var failCounter = 0;

            this.props.allCopies.map(function(copy, index){
                if(copy.status == "fail" ||
                   copy.status == "alert"){
                    failCounter++;
                }

            });
        }
        return failCounter;
    },

	createCopy: function(){
		Actions.createAppCopy();
	},

    render: function(){

//        console.log(this.props)

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

                    <div className="container create-copy" onClick={this.createCopy}>
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
});

export default CopyHistoryFooter;
