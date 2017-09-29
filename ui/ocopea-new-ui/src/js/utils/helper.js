// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
/**
 * Include Helper function in module.exports below !
 */

import moment from 'moment';
import Locations from '../locations.json';
import $ from 'jquery';
import _ from 'lodash';


const Helper = {

  getLocationByPathname: pathname => {
    const location = {};
    const obj = _.deeply(_.mapKeys)(Locations, function (val, key) {
      const path = val.pathname;
      if(path){
        location[path] = val
        return path;
      }
      else return key
    });

    if(!location[pathname]) {
      const paths = _.split(pathname, '/');
      const pathWithNoParams = paths.splice(0, paths.length - 1).join('/');
      return location[pathWithNoParams];
    }

    return location[pathname];
  },

  getDevice: () => {
    return devices[$('body')[0].className];
  },

  getShortName: (string, maxLength) => {
    if(string){
      string = string.toString();
      if(string.length <= maxLength){
        return string;
      }else{
        const substring = string.substring(0, maxLength - 3);
        return substring+'...'
      }
    }
  },

  getTitleOrNull: (string, maxLength) => {
    if(string && string.length > maxLength){
      return string;
    }else{
      return null;
    }
  },

  //returns true if first date is earlier
  //false if first date is later
  compareDates(dateA, dateB){

    var dateA = new DateObject(dateA.getFullYear(),
                   dateA.getMonth()+1,
                   dateA.getDate());

    var dateB = new DateObject(dateB.getFullYear(),
                   dateB.getMonth()+1,
                   dateB.getDate());


    if(dateA.year < dateB.year){
      return true;
    }else if(dateA.year == dateB.year){
      if(dateA.month < dateB.month){
        return true;
      }else if(dateA.month == dateB.month){
        if(dateA.day < dateB.day){
          return true;
        }else{
          return false;
        }
      }
    }

    function DateObject(year, month, day){
      this.year = year;
      this.month = month;
      this.day = day;
    }

  },

  /**
   * formatDate - get formatted date by timestamp:
   * MONTH - DAY_IN_MONTH - YEAR DAY_IN_WEEK HH:MM:SS
   *
   * @param  {string} timestamp description
   * @return {object}           propeties: date, time
   */
  formatDate(timestamp) {
    const stamp = moment(timestamp);
    const year =  stamp.year();
    const monthIndex = stamp.month();
    const month = stamp._locale._monthsShort[monthIndex];
    const dayInMonth = stamp.date();
    const dayInWeek = stamp._locale._weekdaysMin[stamp.day()];
    const time = stamp.format('HH:mm:ss A');
    const date = `${month} ${dayInMonth} ${year} ${dayInWeek}`;
    return {date: date, time: time};
  },

  /**
   * toggleFilter - add or remove filter
   *
   * @param  {String}   type                selected filter type
   * @param  {object}   filterName          selected filter object, contains name and id
   * @param  {Array}    selectedFilters     array of strings, contains selected filters name space
   * @param  {object}   allFiltersObject    object describing all filters, property key is filter name.
   *                                        ex. [ filterName: { name: 'filterName', items: [] } ]
   * @param  {func}     handler             returns true if condition set in this function is met
   * @param  {Boolian}  forbidMultiple      disables multiple selection if set to true
   */
  toggleFilter(type, filterName, allFilters, allFiltersObject, arrayToFilter, handler, forbidMultiple) {
    // populate model handler allFilters observalble array
    populateAllFilters(allFilters, filterName, type, forbidMultiple);
    // populate model handler filter items observalble array
    populateFIlterItems(forbidMultiple, filterName, type, allFiltersObject);
    // filter store filtered array and return array of model objects to handler
    const filtered = _.filter(arrayToFilter, _filter.bind(this, allFiltersObject, handler, allFilters));

    return filtered;
  },

}

function populateAllFilters(allFilters, filterName, type, forbidMultiple) {
  const hasFilter = _.filter(allFilters, filter=>{return filter.name === filterName })[0];
  const filter = {name: filterName, type: type};

  // filter does not exist
  if(!hasFilter){
    // multiple allowed
    if(!forbidMultiple){
      allFilters.push(filter);
    }else{
      // multiple not allowed
      if(!allFilters.length){
        // first uniqe
        allFilters.push(filter);
      }else{
        // has uniqe item replace with new one
        _.remove(allFilters, (f)=>{ return f.type === type });
        allFilters.push(filter);
      }
    }
  }else{
    // filter exists, remove it
    _.remove(allFilters, f=>{ return f.name === filterName });
  }
}

function populateFIlterItems(forbidMultiple, filterName, type, allFiltersObject) {
  if(!forbidMultiple){
    // allow multiple selections
    if(allFiltersObject[type].selectedItems.indexOf(filterName) === -1){
      allFiltersObject[type].selectedItems.push(filterName);
    }else{
      allFiltersObject[type].selectedItems.remove(filterName);
    }
  }else{
    // allow only one selection
    if(allFiltersObject[type].selectedItems.indexOf(filterName) === -1){
      allFiltersObject[type].selectedItems[0] = filterName;
    }else{
      allFiltersObject[type].selectedItems = [];
    }
  }
}


function _filter(allFiltersObject, handler, allFilters, object) {

  var check = false;

  for (let filter in allFiltersObject){
    // console.log('handler: ', handler)
    // console.log('allFiltersObject: ', allFiltersObject)
    // console.log('filter: ', filter)
    // console.log('object[filter]: ', object[filter])
    if (allFiltersObject[filter].selectedItems.length > 0){
        allFiltersObject[filter].selectedItems.forEach(item=>{
          if(handler(object, filter, item, allFilters)){
            check = true;
          }
        });
        if (!check){
            return false
        }
    }
    check = false
  }
  return true
}

_.mixin({
    deeply: function (map) {
        return function(obj, fn) {
            return map(_.mapValues(obj, function (v) {
                return _.isPlainObject(v) ? _.deeply(map)(v, fn) : v;
            }), fn);
        }
    },
});

const devices = {
  mobile: 'mobile',
  tablet: 'tablet',
  desktop: 'desktop'
}

module.exports = {
  devices: devices,
  getDevice: Helper.getDevice,
  getLocationByPathname: Helper.getLocationByPathname,
  getShortName: Helper.getShortName,
  getTitleOrNull: Helper.getTitleOrNull,
  formatDate: Helper.formatDate,
  toggleFilter: Helper.toggleFilter,
  compareDates: Helper.compareDates
}
