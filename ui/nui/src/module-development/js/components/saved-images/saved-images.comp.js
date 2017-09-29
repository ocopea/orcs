// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import $ from 'jquery';

//actions
import ProdActions from '../../../../module-production/js/actions/actions.js';
import DevActions from '../../actions/dev-actions.js';
import DevDashboardActions from '../../actions/dev-dashboard-actions.js';
import DevWizardActions from '../../actions/dev-wizard-actions.js';
import SharedActions from '../../../../shared-actions.js';
import AppTopologyActions from '../../../../module-production/js/actions/appTopologyActions.js';

//components
import SortableTable from '../../../../shared-components/js/sortable-table.comp.js';
import SearchSelect from './search-select.comp.js';
import TagsTooltip from '../wizard/image/savedImage.tagsTooltip.comp.js';

// helpers
import Config from '../../../../module-production/js/config.js';
import DevNavigationOptions from '../../data/devNavigationOptions.js';
var GeminiScrollbar = require('react-gemini-scrollbar');


var SavedImages = React.createClass({

  componentDidMount: function(){
    ProdActions.closeLeftMenu();

    //hide tags tooltip on document click
    var that = this;
    $(document).click(function(e){
      if(that.props.tooltip.isRender){
        if(!e.target.classList.contains('tags-tt')){
          DevWizardActions.hideTagsTooltip();
        }
      }
    });
  },

  getTableHeader: function(){
    return [
      {
        'content':'',
        'style':{'width':'11%'}
      },
      {
        'content':'owner',
        'onClick': DevDashboardActions.sortImageOriginByUserName.bind(this, 'owner'),
        'colSpan':2,
        'style':{'width':'20%'}
      },
      {
        'content':'name',
        'onClick': DevDashboardActions.sortImageOrignByImageName.bind(this, 'name'),
        'style':{'width':'15%'}
      },
      {
        'content':'created',
        'onClick': DevDashboardActions.sortImageOriginByDate.bind(this, 'created'),
        'style':{'width':'14%'}
      },
      {
        'content':'tags',
        'onClick':function(){},
        'colSpan':2,
        'style':{'width':'40%'}
      }
    ]
  },

  getTableBody: function(){
   var body = this.props.filteredImages.map((image, i)=>{

     var user = this.props.users[image.createdByUserId],
         appTemplateImg = "";

     if(!_.isEmpty(this.props.appTemplates) && image.appTemplateId !== ""){
       appTemplateImg = this.props.appTemplates[image.appTemplateId].img;
     }

     return{
       'tr': {},
       'td': [
         {
           'cell':{
             'content':<img
                         src={appTemplateImg}
                         className="Saved-images__table__tbody__tr__td__app-template-img" />,
             'attr':{
               'style':{'textAlign':'left', 'paddingLeft':'25px'}
             }
           }
         },
         {
           'cell':{
             'content': <img
                         src={APISERVER+`/hub-web-api/user/${image.createdByUserId}/avatar`}
                         className={'Saved-images__table__tbody__tr__td__avatar'} />
           }
         },
         {
           'cell':{
             'content': user !== undefined ?
                        Config.getShortName(`${user.firstName} ${user.lastName}`, 15) : null
           }
         },
         {
           'cell':{
             'content': Config.getShortName(image.name, 20)
           }
         },
         {
           'cell':{
             'content': image.dateCreated === undefined ?
                        Config.parseDate(image.creationTime) :
                        Config.parseDate(image.dateCreated)
           }
         },
         {
           'cell':{
             'content': this.getTags(image.tags),
             'attr':{
               'style':{'width': '20%'}
             }
           }
         },
         {
           'cell':{
             'content': this.getIcons(image, i)
           }
         },
       ]
     }
   });
   return body;
  },

  userClickOnRunApp(image, index){
    // navigate to wizard config with selectedApp and selectedImage
    var appTemplate = this.props.appTemplates[image.appTemplateId];
    DevWizardActions.setIsFromSavedImages(true);
    DevWizardActions.setCurrentStep(DevNavigationOptions.wizard.subLocation.config);
    DevWizardActions.userSelectedApp(appTemplate);
		AppTopologyActions.setSelectedApp(appTemplate);
    DevWizardActions.userSelectedImage(index);
    DevWizardActions.invalidate();
    SharedActions.navigate({
      module: DevNavigationOptions.module,
      location: DevNavigationOptions.wizard.location,
      subLocation: DevNavigationOptions.wizard.subLocation.config
    });
  },

  userClickOnInfo(image, index){
    var appTemplate = this.props.appTemplates[image.appTemplateId];
    DevWizardActions.userSelectedApp(appTemplate);
    DevWizardActions.userSelectedImage(index);
    DevWizardActions.showCopyDetails(index);
  },

  getIcons: function(image, index){
    return(
      <div className="Saved-images__table__tbody__tr__td__icons-container">
        <span
          onClick={this.userClickOnInfo.bind(this, image, index)}
          className={"icon-info "+
                         "link "+
                         "Saved-images__table__tbody__tr__td__icons-container__icon"}></span>
        <span
          onClick={this.userClickOnRunApp.bind(this, image, index)}
          className={"icon-play "+
                         "link "+
                         "Saved-images__table__tbody__tr__td__icons-container__icon"}></span>
        <span
          onClick={DevActions.showShareImageDialog.bind(this, image)}
          className={"icon-share "+
                     "link "+
                     "Saved-images__table__tbody__tr__td__icons-container__icon"}></span>
        <span className={"icon-delete "+
                         "link "+
                         "Saved-images__table__tbody__tr__td__icons-container__icon "+
                         "Saved-images__table__tbody__tr__td__icons-container__icon--last"}></span>
      </div>
    )
  },

  showTagsTooltip: function(tags, e){
    var position = {
      top: $(e.target).position().top - 55,
      left: $(e.target).position().left
    }
    DevWizardActions.showTagsTooltip(position, tags)
  },

  getTags: function(tags){
    var tagsElements = tags.map((tag, i)=>{
      if(i < 2){
        return(
          <div
            key={i}
            className="Saved-images__table__tbody__tr__td__tag tag">
              {Config.getShortName(tag, 4)}
          </div>
        )
      }else if(i==2){
        return(
          <div key={i}
            className="more-tags tags-tt"
            onClick={this.showTagsTooltip.bind(this, tags)}>
              <span className="tags-tt"></span>
              <span className="tags-tt"></span>
              <span className="tags-tt"></span>
          </div>
        )
      }
    });
    return tagsElements;
  },

  focusSearchSelect: function(){
    $('.Select-input input').focus();
  },

  render: function(){

    return(
      <GeminiScrollbar>

      <div className="Saved-images">
          <div className="Saved-images__title">
            saved images
          </div>
          <div className="Saved-images__sub-title">
            your saved images of all applications
          </div>
          <div className="Saved-images__search">
            <SearchSelect
              options={this.props.images}/>
            <span className="Saved-images__search__icon-search icon-search"
              onClick={this.focusSearchSelect}></span>
          </div>
            <SortableTable
              header={this.getTableHeader()}
              body={this.getTableBody()}
              rootClassName='Saved-images__table'
              sortBy={this.props.sortBy}/>
          {
            this.props.tooltip.isRender ?
              <TagsTooltip
                data={this.props.tooltip}
              />
            :
            null
          }

      {/* /Saved-images */}
      </div>
      </GeminiScrollbar>

    )
  }
});

SavedImages.propTypes = {
  images: React.PropTypes.array.isRequired,
  appTemplates: React.PropTypes.object.isRequired,
  users: React.PropTypes.object.isRequired,
  sortBy: React.PropTypes.string.isRequired
}

export default SavedImages;
