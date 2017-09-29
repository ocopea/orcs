// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import $ from 'jquery';
import _ from 'lodash';
import moment from 'moment';


let Config = {

		fixImgUrl: function(url){
			if(url){
				var isDev = APISERVER.length > 0;
				if(!isDev){
					var fixedUrl = url.replace("../", APISERVER+"/hub-web-api/html/");
					return fixedUrl;
				}else{
					var dev_server_host = 'http://localhost:8083';
					var u;
					url.indexOf('localhost') !== -1 ? u = url : u = `${dev_server_host}${url}`;
					return u;
				}
			}
		},

		getWebEntryPointURL: function(appName){
			return `${APISERVER}/${appName}/hackathon-api/html/nui/index.html`
		},

    getLocationFromHash: function(hash){
        var location;
        if(hash != undefined && hash.indexOf('/') != -1){
            return hash.substring(0, hash.indexOf('/'));
        }else{
            return hash;
        }
    },

    getCurrentHash: function(){

      var hash = window.location.hash;
      hash = hash.substring(1);

			var splitHash = _.split(hash, '/');

			var module = splitHash[0],
				location = splitHash[1],
				subLocation = splitHash[2],
				additional = splitHash[3];

      var currentLocation = {
		      module: module,
          location: location,
          subLocation: subLocation,
					additional: additional
      }

      return currentLocation

    },

	//returns true if first date is earlier
	//false if first date is later
    compareDates: function(dateA, dateB){

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

	//make string which length is longer that 6 character shorter
	//and append three dots
	getShortName: function(name, maxLength){

		if(maxLength == undefined){
			maxLength = 10;
		}

		if(name && name.length > maxLength){
			return name.substring(0,maxLength)+"..."
		}else{
			return name;
		}

	},

	parseDate: function(timeStamp){
    var date = new Date(timeStamp),
        day = moment(timeStamp).format('D'),
        month = date.getMonth(),
        monthName = moment()._locale._monthsShort[month],
        year = date.getFullYear(),
        fullDate = `${day} ${monthName} ${year}`;

    return fullDate;
  },

	//check if string has only numbers
	onlyNumbers: function(string){
		return /^\d+$/.test(string)
	},

	//check if string starts with number
	startsWithNumber: function(string){
		return /^\d/.test(string)
	},

	//remove white spaces from string
	removeWhiteSpaces: function(string){
		return string.replace(/ /g,'').replace(/[^\w\s]/gi, '');
	},

	objectifyArrayByParam: function(array, param){
		return _.forEach(array, o=>{
			return this[o.param] = o
		});
	},

	getTitleOrNull: function(title, max_length){
		return 	title !== undefined &&
						title.length > max_length ? title : null;
	},

	//general function for POST or GET requests
	request: function(options, successHandler, errorHandler){

		$.ajax(
			{
				url: options.url,
				success: (response, textStatus, xhr)=>{
					successHandler(response);
				},
				error: err=>{
					errorHandler(err)
				},
				contentType: options.contentType,
				type: options.method,
				data: JSON.stringify(options.data)
	    }
		);

	}

};

export default Config;
