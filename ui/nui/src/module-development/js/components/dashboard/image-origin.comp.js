// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
import DevDashboardActions from '../../actions/dev-dashboard-actions.js';
import _ from 'lodash';
import moment from 'moment';


var ImageOrigin = React.createClass({

  componentDidMount: function(){
    var images = this.props.savedImages;
    this.setComponentState();
  },

  componentDidUpdate: function(nextProps){

    if(this.props.savedImages !== nextProps.savedImages){
      this.setComponentState();
    }
  },

  setComponentState: function(){
    var images = this.props.savedImages;
    this.setState({
      images: images,
      firstFour: _.slice(images, 0, 4)
    });
  },

  parseDate: function(timeStamp){
    var date = moment(timeStamp),
        day = date.date(),
        month = moment()._locale._monthsShort[date.month()],
        year = date.year();

    return `${day} ${month} ${year}`
  },

  getCreatorInfo: function(){
    var that = this;
    
    if(this.state !== null && !_.isEmpty(this.props.users)){
      var items = this.state.firstFour.map((image, i)=>{

        var user = that.props.users[image.createdByUserId];
        var className = i <= 1 ?
          'Dashboard__inside__image-origin__inside__items__item--left' :
          'Dashboard__inside__image-origin__inside__items__item--right';
        var modifier = i == 0 ? '--first' : i == that.props.savedImages.length-1 ? '--last' : '';

          return(
              <div
                key={i}
                className={`Dashboard__inside__image-origin__inside__items__item ${className}${modifier}`}>
                  <div className="Dashboard__inside__image-origin__inside__items__item__avatar-container">
                    <img
                      className="Dashboard__inside__image-origin__inside__items__item__avatar-container__image"
                      src={APISERVER + `/hub-web-api/user/${image.createdByUserId}/avatar`} />
                  </div>
                  <div className="Dashboard__inside__image-origin__inside__items__item__name">
                    {`${user.firstName} ${user.lastName}`}
                  </div>
                  <div className="Dashboard__inside__image-origin__inside__items__item__date-created">
                    {that.parseDate(image.dateCreated)}
                  </div>
              </div>
          )
      });
      return items;
    }

  },

  seeAll: function(){
    if(this.props.savedImages.length > 3){
      return(
        <div className="Dashboard__inside__image-origin__inside__items__see-all">
          <hr className="Dashboard__inside__image-origin__inside__items__see-all__line"></hr>
          <span className="Dashboard__inside__image-origin__inside__items__see-all__span-container">
            <span
                className="Dashboard__inside__image-origin__inside__items__see-all__span-container__span"
                onClick={DevDashboardActions.showImageOriginList}>
              see all images
            </span>
          </span>
        </div>
      )
    }else{
      return null;
    }
  },

  render: function(){
    //console.log(this.props)
    return(
      <div className={'Dashboard__inside__image-origin ' +
                     'Dashboard__inside__card'}>

        <div className='Dashboard__inside__card__title'>
          <span className="Dashboard__inside__card__title__span">image origin</span>
        </div>

        <div className={'Dashboard__inside__image-origin__inside ' +
                       'Dashboard__inside__card__inside'}>
            <div className={'Dashboard__inside__image-origin__inside__items'}>
              {this.getCreatorInfo()}
              {this.seeAll()}
            </div>

        {/* /Dashboard__inside__image-origin__inside */}
        </div>
      </div>
    )
  }

});

export default ImageOrigin;
