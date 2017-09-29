// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevWizardActions from '../../../actions/dev-wizard-actions.js';
import MockAppImg from '../../../../../module-production/assets/images/cards/hackathon.png';
import Topology from './savedImage.copyDetails.topology.comp.js';
import Footer from './savedImage.copyDetails.footer.comp.js';
import Config from '../../../../../module-production/js/config.js';
import TopologyParser from '../../../../../shared-components/js/topology-data-parser.js';
import AppTopologyActions from '../../../../../module-production/js/actions/appTopologyActions.js';
import moment from 'moment';
import _ from 'lodash';
var GeminiScrollbar = require('react-gemini-scrollbar');

var CopyDetails = React.createClass({

  getInitialState: function(){
    return {
      template: {}
    }
  },

  componentDidMount: function(){
    var appTemplates = this.props.applications,
        imageDetails = this.props.imageDetails.details,
        template = appTemplates[imageDetails.appTemplateId];
  },

  componentWillUnmount: function(){
    AppTopologyActions.setSelectedApp({})
  },

  getUserAvatarURL: function(user){
    return APISERVER + `/hub-web-api/user/${user.id}/avatar`;
  },

  parseDate: function(timeStamp){
    var date = new Date(timeStamp),
        hours = date.getHours(),
        minutes = date.getMinutes(),
        day = date.getDate(),
        month = moment()._locale._monthsShort[date.getMonth()],
        year = date.getFullYear();

    return `${day}-${month}-${year} ${hours}:${minutes}`;
  },

  render: function(){

    var users = this.props.users,
        selectedImage = this.props.selectedImage,
        createdByUserId = selectedImage.createdByUserId,
        appTemplateId = selectedImage.appTemplateId,
        selectedApp = this.props.imageDetails.details,
        creatorFullName = `${users[createdByUserId].firstName} ${users[createdByUserId].lastName}`,
        template = this.props.applications[appTemplateId];

    return(

      <div className="copy-details">

        <div className="title">
          <span>copy details</span>
          <div className="close-btn"
            onClick={DevWizardActions.hideCopyDetails}>
              <span></span>
              <span></span>
          </div>
        </div>

        <div className="upper-container">

          <section className="application">
            <div className="inside">
              <div className="icon"><img src={MockAppImg} /></div>
              <div className="description">
                <span
                  title={Config.getTitleOrNull(template.name, 10)}
                  className="app-name">
                    {Config.getShortName(template.name, 10)}</span>
                <span
                  title={Config.getTitleOrNull(this.props.selectedImage.name, 13)}
                  className="image-name">
                    {Config.getShortName(this.props.selectedImage.name, 13)}</span>
              </div>
            </div>
          </section>

          <section className="creator">
            <div className="inside">
              <div className="icon">
                <img src={this.getUserAvatarURL(users[createdByUserId])} />
              </div>
              <div
                className="description"
                title={creatorFullName.length > 13 ? creatorFullName : null}>
                  <span className="name">
                    {Config.getShortName(creatorFullName, 13)}
                  </span>
                  <span className="date-created">{this.parseDate(selectedImage.creationTime)}</span>
              </div>
            </div>
          </section>

          <section className="info">
            <div className="inside">
              <span className="size">size: { this.props.selectedImage.size}</span>
              <span className="restore-time">
                restore time: { this.props.selectedImage.restoreTime}
              </span>
            </div>
          </section>

        {/* /upper-container */}
        </div>

        <div className="inside">

          <div className="sub-title">
            <span>copy topology</span>
            <div className="legend">

              <div className="application">
                <div className="inside">
                  <span className="app-i"></span>
                  <span>application</span>
                </div>
              </div>

              <div className="service">
                <div className="inside">
                  <span className="service-i"></span>
                  <span>service</span>
                </div>
              </div>

            {/* /legend */}
            </div>
          {/* /sub-title */}
          </div>

          <Topology
              selectedApp={selectedApp}
              selectedStructure={this.props.selectedStructure}/>

          <Footer selectedStructure={this.props.selectedStructure}/>

        {/* /inside */}
        </div>
      {/* /copy-details */}
      </div>
    )
  }
});

export default CopyDetails;
