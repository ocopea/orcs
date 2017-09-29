// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import Config from '../../../../../module-production/js/config.js';
import Isvg from 'react-inlinesvg';
import InfoIcon from '../../../../assets/images/wizard/image/info-icon.svg';
import TagsTooltip from './savedImage.tagsTooltip.comp.js';
import DevWizardActions from '../../../actions/dev-wizard-actions.js';
import $ from 'jquery';
import _ from 'lodash';
import moment from 'moment';


var SavedImageTable = React.createClass({

  componentDidMount: function(){

    if(_.isEmpty(this.props.image.selectedImage)){
        DevWizardActions.userSelectedImage(null);
        $(".image-row").removeClass('selected');
    }

    DevWizardActions.hideTagsTooltip();
  },

  userClickOnShowMoreTags: function(e){
    DevWizardActions.hideTagsTooltip();
    var position = $(e.target).offset(),
        tags = this.props.image.savedImageData[$(e.target).closest("tr")[0].id].tags;
    DevWizardActions.showTagsTooltip(position, tags);
  },

  getTag: function(tags){

    var that = this;
    var tagsElements = tags.map((tag, i)=>{

      if(i<2){
        return  <div className="tag" key={i} title={tag.length > 4 ? tag : null}>
                  <span>{Config.getShortName(tag, 4)}</span>
                </div>
      }else if(i==2){
        return  <div className="more-tags" onClick={this.userClickOnShowMoreTags} key={i}>
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
      }
    });
    return tagsElements;
  },

  userClickOnImageInfo: function(e){
    var imageId = $(e.target).closest('tr')[0].id;
    DevWizardActions.showCopyDetails(imageId);
  },

  getDate: function(date){
    var day = new Date(date).getDate(),
        month = new Date(date).getMonth(),
        monthName = moment()._locale._monthsShort[month],
        year = new Date(date).getFullYear();

    return `${day} ${monthName} ${year}`
  },

  getCreatorUserName: function(creatorID){
    var user = this.props.users[creatorID];
    if(user !== undefined){
      var fullName = `${user.firstName} ${user.lastName}`;
      return fullName;
    }
  },

  userSelectedImage: function(id, e){
    if(e.currentTarget.className == 'image-row'){
      DevWizardActions.userSelectedImage(id);
    }
  },

  getTableRows: function(){

      var rows = this.props.image.savedImageData.map((obj, i)=>{
        return  <tr
                    key={i}
                    id={i}
                    className={this.props.image.selectedImage.index == i ?
                               "image-row selected" : "image-row"}
                    onClick={this.userSelectedImage.bind(this, i)}>
                  <td width="30px">
                      <img
                        src={APISERVER+"/hub-web-api/user/"+obj.createdByUserId+"/avatar"}
                        className="avatar"/>
                      <div className="icon-check-container">
                        <span className="icon-check"></span>
                      </div>
                  </td>
                  <td style={{paddingLeft: "11px"}}>
                    {this.getCreatorUserName(obj.createdByUserId)}
                  </td>
                  <td>{Config.getShortName(obj.name, 14)}</td>
                  <td>{Config.getShortName(this.getDate(obj.creationTime), 14)}</td>
                  <td width="150px">{this.getTag(obj.tags)}</td>
                  <td width="60px">
                    <span className="icon-info link" onClick={this.userClickOnImageInfo}></span>
                  </td>
                </tr>
      });

      return rows;
  },

  render: function(){

    return(
      <div>
        {
          this.props.image.tooltip.isRender ?
            <TagsTooltip data={this.props.image.tooltip}/>
          :
          null
        }
        <table>
          <thead>
            <tr>
              <th colSpan="2">owner</th>
              <th>name</th>
              <th>created</th>
              <th colSpan="2" width="160px">tags</th>
            </tr>
          </thead>
          <tbody>
            {this.getTableRows()}
          </tbody>
        </table>
      </div>
    )
  }
});

export default SavedImageTable;
