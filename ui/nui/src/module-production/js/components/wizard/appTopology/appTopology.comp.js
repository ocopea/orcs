// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
import React from 'react';
var d3 = require('d3');
import _ from 'lodash';
import $ from 'jquery';
import Actions from '../../../actions/appTopologyActions.js';
import SharedActions from '../../../../../shared-actions.js';
import lines from '../../../appTopology/lineFunctions.js';
import classNames from '../../../appTopology/classNames.js';
import serviceIcon from '../../../../assets/images/appTopology/app.svg';
import dbIcon from '../../../../assets/images/appTopology/db.svg';
import LabelA from '../../../../assets/images/labels/label-a.png';
import LabelB from '../../../../assets/images/labels/label-b.png';
import DevNavigationOptions from '../../../../../module-development/js/data/devNavigationOptions.js';
import BrowserDetector from '../../../browserDetection.js';
import Config from '../../../config.js';
import AppendErrorIndicator from './errorIndicator.js';


var Isvg = require('react-inlinesvg');


let AppTopology = React.createClass({

    isDevModule: function(){
      return this.props.module == DevNavigationOptions.module;
    },

    componentDidUpdate: function(nextProps){

  		var selectedService = this.props.state.services.selectedService
  		var relatedDependencies = selectedService.relatedDependencies;

  		var selectedDependency = this.props.state.dependencies.selectedDependency
  		var relatedServices = selectedDependency.relatedServices;

      var isServiceSelected = !_.isEqual(this.props.selectedService, {}),
          isDependencySelected = !_.isEqual(this.props.selectedDependency, {});

      //service has been selected
      if(isServiceSelected && this.props.selectedService != nextProps.selectedService ||
	       relatedDependencies.length > 0){
            this.removeSelectedPath();
            this.clickOnService();
      }

      //dependency has been selected
      if(isDependencySelected &&
           this.props.selectedDependency != nextProps.selectedDependency ||
           relatedServices.length > 0){
              this.removeSelectedPath();
              this.clickOnDependency();
      }

  		//dependencies translate updated
  		if(this.props.dependenciesTranslate != nextProps.dependenciesTranslate){
              this.translateDependencies(this.props.dependenciesTranslate);
  		}

      //services translate updated
      if(this.props.servicesTranslate != nextProps.servicesTranslate){
          this.translateServices(this.props.servicesTranslate);
      }

      // if(!this.initiate && this.props.currentStepName == "show"){
      //   // console.log(this.params().collections)
      //     if(!_.isEmpty(this.props.selectedApp) &&
      //        this.params().collections.services.length > 0 &&
      //        this.params().collections.dependencies.length > 0){
      //
      //         // this.init();
      //         this.initiate = true;
      //     }
      // }
      // // console.log(this.props)
      // if(this.props.allServices !== nextProps.allServices ||
	    //     this.props.selectedApp !== nextProps.selectedApp){
      //
      //     if(this.params().collections.services.length > 0 &&
	    //        this.params().collections.dependencies.length > 0){
      //             // Actions.initialAppTopology();
      //             // this.init();
      //             // this.initiate = false;
      //     }
      // }
      
      if(this.props.configuration !== nextProps.configuration ||
         this.props.allServices !== nextProps.allServices){
        this.init();
      }
    },

    componentDidMount() {
      this.init();
    },

    componentWillUnmount() {
      Actions.initializeSelectedElement();
      // Actions.initialAppTopology();
    },

    clickOnService: function(){
      //handle selection highlight
      this.removeSelectedDependencyHighlight();
      this.highlightSelectedService();
      //line from selected service to baseline
      var selectedServiceToBaseline = lines.pathFromSelectedServiceToBaseline(
													this.props.selectedService,
													this.params()
									);

      var className = classNames.serviceToBaseline + " " +
					classNames.serviceSelected;

      this.drawLine(selectedServiceToBaseline, this.selectedPathParams(className));

  		//line from first to last related dependency
  		var relatedDependencies = this.props.state.services.selectedService.relatedDependencies,
  			firstRelatedDependency = relatedDependencies[0],
  			lastRelatedDependency = _.last(relatedDependencies),
  			firstToLastRelatedDependency = lines.pathFromFirstToLastRelatedDependencies(
  																		firstRelatedDependency,
  																		lastRelatedDependency,
  																		this.params()),
          	className = classNames.firstToLastDependency + " " +
  						classNames.serviceSelected;

  		this.drawLine(firstToLastRelatedDependency, this.selectedPathParams(className));

  		//lines from baseline to related dependencies
  		this.props.state.services.selectedService.relatedDependencies.forEach((dependency)=>{

			var baselineToRelatedDependency =
							lines.pathFromBaseLineToRelatedDependency(
																	dependency,
																	this.params()
																);
			var className = classNames.baselineToDependency + " " +
							classNames.serviceSelected;

			this.drawLine(baselineToRelatedDependency, this.selectedPathParams(className));

		});

		//line from selected service to closest dependency
		var selectedService = this.props.state.services.selectedService,
			firstRelatedDependency = selectedService.relatedDependencies[0],
			lastRelatedDependency = _.last(selectedService.relatedDependencies),
			closestRelatedDependency = this.getClosestDependency(
                                            this.props.selectedService,
                                            firstRelatedDependency, lastRelatedDependency
									   ),
			dependenciesTranslate = this.props.dependenciesTranslate,
			servicesTranslate = this.props.servicesTranslate;

        //hack- to be solved in this.getClosestDependency()
        closestRelatedDependency =
			closestRelatedDependency == undefined ? firstRelatedDependency : closestRelatedDependency;

		var serviceToClosestDependency =
				lines.pathFromServiceToClosestRelatedDependency(
					selectedService,
					firstRelatedDependency,
					lastRelatedDependency,
					closestRelatedDependency,
					dependenciesTranslate,
					servicesTranslate,
					this.params()
				);

		this.drawLine(serviceToClosestDependency,
					  this.selectedPathParams(classNames.serviceToClosestDependency));


		//handle translation
        this.translateDependencies();
        this.translateServices();

    },

    clickOnDependency: function(){
		//handle selection highlight
        this.removeSelectedServiceHighlight();
        this.highlightSelectedDependency();

		//line from selected dependency to baseline
    		var selectedDependencyToBaseline = lines.pathFromSelectedDependencyToBaseLine(
    												this.props.selectedDependency,
    												this.params()
    										   );

        var className = classNames.dependencyToBaseline + " " +
                        classNames.dependencySelected;

        this.drawLine(selectedDependencyToBaseline, this.selectedPathParams(className));

		//line from related services to baseline
		var selectedDependency = this.props.state.dependencies.selectedDependency,
					relatedServices = selectedDependency.relatedServices;

		relatedServices.forEach((service)=>{
			var relatedServiceToBaseline = lines.pathFromRelatedServiceToBaseline(service, this.params());

            var className = classNames.serviceToBaseline + " " +
                            classNames.dependencySelected;

            this.drawLine(relatedServiceToBaseline, this.selectedPathParams(className));
		});

		//line from first to last related service
    if(relatedServices[0]){
      var firstToLastRelatedService = lines.pathFromFirstToLastRelatedServices(
  											relatedServices[0],
  											_.last(relatedServices),
  											this.params()
  										);
      var className = classNames.firstToLastService + " " +
                      classNames.dependencySelected;

      this.drawLine(firstToLastRelatedService, this.selectedPathParams(className));

      //line from selected dependency to closest service
  		var closestService = this.getClosestElement(
  											selectedDependency.dependency,
  											relatedServices[0],
  											_.last(relatedServices)
  							);

  		var selectedDependencyToClosestService = lines.pathFromSelectedDependencyToClosestService(
  														selectedDependency.dependency,
  														closestService,
  														this.props.dependenciesTranslate,
  														this.props.servicesTranslate,
  														this.params()
  												 );
  		var className = classNames.dependencyToClosestService + " " +
  						classNames.dependencySelected;

  		this.drawLine(selectedDependencyToClosestService, this.selectedPathParams(className));

    }

		  //handle translation
      this.translateDependencies();
      this.translateServices();
    },

    init: function(){

      d3.select(".app-topology-container svg").remove();
      // if(d3.select(".app-topology-container svg")[0][0] === null){
        var proportions = this.params().proportions;

        //main svg container
        var svg = d3.select(".app-topology-container").append("svg")
                    .attr("width", proportions.containerWidth)
                    .attr("height", proportions.containerHeight);

        var border = svg.append("rect")
                        .attr("width", proportions.containerWidth)
                        .attr("height", proportions.containerHeight)
                        .attr("fill", "none");

        this.appendServicesAndDependencies(svg);
      // }
    },

    childrenHasClass(object, className){
      let hasIcon = false;
      _.forEach(object, child=>{
        if(child.classList.contains(className)){
          hasIcon = true;
          return true;
        }
      });
      return hasIcon;
    },

    appendServicesAndDependencies: function(svg){

        var that = this;
        var proportions = this.params().proportions;

        //append services
        this.servicesContainer = svg.append("g")
                                    .data([{translate: this.props.state.services, type: "services"}])
                                    .attr("class", "services-container");

        var serviceSize = this.isDevModule() ? 134 : proportions.boxSize;
        var serviceImgSize = this.isDevModule() ? 60 : proportions.boxSize / 2;

        var serviceBorder = this.servicesContainer
                                .selectAll("g")
                                .data(this.props.state.application.allServices)
                                .enter()
                                    .append("g")
                                    .attr("class","service")
                                    .attr("id", function(d){return d.serviceId})
                                    .append("rect")
                                    .attr("width", serviceSize)
                                    .attr("height", serviceSize)
                                    .attr("x", function(d){ return d.x } )
                                    .attr("y", proportions.servicesHeight )
                                    .attr("rx", 5 )
                                    .attr("ry", 5 )
                                    .on("click", Actions.userClickOnAppTopologyService.bind(this))

        if(this.isDevModule() && this.props.configuration){
          AppendErrorIndicator(
            d3.selectAll('g.service'),
            this.props.configuration.appServiceConfigurations,
            'appServiceName',
            'no-versions',
            'supportedVersions',
            'circle',
            'no versions found',
            'service'
          );
        }

        var serviceImg =  d3.selectAll(".service")
                            .append("svg:image")
                            .attr("xlink:href",function(d){ return d.img })
                            .attr("width", serviceImgSize)
                            .attr("height", serviceImgSize)
                            .attr("class", "service" )
                            .attr("x", function(d){ return d.x + serviceImgSize/2} )
                            .attr("y", proportions.servicesHeight + serviceImgSize/2 - 10)
                            .on("click", Actions.userClickOnAppTopologyService.bind(this))
                            .append("svg:title")
                                .text(function(d){
                                    return "name: " + d.name
                                });

        if(!this.isDevModule()){
          var iconContainer = d3.selectAll("g.service")
                      .on("click", Actions.userClickOnAppTopologyService.bind(this))
                      .append("circle")
                      .attr("class", classNames.iconContainer)
                      .attr("cx", function(d){ return d.x } )
                      .attr("cy", proportions.servicesHeight )

          var serviceTitle = d3.selectAll("g.service")
                              .append('foreignObject')
                              .html((d)=>{return Config.getShortName(d.appServiceName, 10)})
                              .attr({
                                class: 'service-title',
                                x: d=>{return d.x},
                                y: proportions.servicesHeight +
                                    proportions.boxSize - 20,
                                'font-size': '13px',
                                color: '6b7083',
                                width: proportions.boxSize
                              })
                              .append("svg:title")
                                .text(function(d){
                                  return d.appServiceName
                              });

        }

        var iconX = !this.isDevModule() ? 12 : 9,
            iconY = !this.isDevModule() ? 12 : 30;

        if(this.isDevModule() && this.props.configuration){
          AppendErrorIndicator(
            d3.selectAll('g.service'),
            this.props.configuration.appServiceConfigurations,
            'appServiceName',
            'no-versions-triangle',
            'supportedVersions',
            'triangle',
            'no versions found',
            'service'
          );
        }

        $("g.service").each(function(index, element){
          let elementX = 0;
          d3.select(element).each((d)=>{
            elementX = d.x;
          });
          const params = that.params();

          let g = d3.select(element).append('g').attr({
            "font-size": "25px"
          });
          g.append("foreignObject")
          .attr({
            "x": elementX - iconX,
            "y": params.proportions.servicesHeight - iconY,
            "class": "icon-service",
            "width": "30px",
            "height": "30px",
          })
          .on("click", Actions.userClickOnAppTopologyService.bind(this));;
        })

        // d3.xml(serviceIcon).mimeType("image/svg+xml").get(function(error, xml) {
        //   if (error) throw error;
        //
        //   $("g.service").each(function(index, element){
        //     var importedNode = document.importNode(xml.documentElement, true);
        //     var hasClass = false;
        //
        //     var elementXvalue = $(element).children()[0].attributes.x.value,
        //         elementYvalue = $(element).children()[0].attributes.y.value;
        //
        //     importedNode.setAttribute("x", elementXvalue - iconX)
        //     importedNode.setAttribute("y", elementYvalue - iconY)
        //     importedNode.setAttribute("class", "icon")
        //
        //     _.forEach($(element), child=>{
        //       _.forEach(child.children, child=>{
        //         if(child.classList.contains('icon')){
        //           hasClass = true;
        //           return;
        //         }
        //       })
        //     });
        //
        //     // if(!hasClass){
        //         element.appendChild(importedNode);
        //     // }
        //
        //     d3.select(element)
        //     .on("click", Actions.userClickOnAppTopologyService.bind(this));
        //   });
        // });

        this.servicesContainerWidth = this.servicesContainer[0][0].getBoundingClientRect().width;

        //append dependencies
        this.dependenciesContainer = svg.append("g")
                                    .data([{translate: this.props.state.dependencies, type: "dependencies"}])
                                    .attr("class", "dependencies-container");

        var dependencyBorder = this.dependenciesContainer
                                .selectAll("g")
                                .data(this.props.state.application.allDependencies)
                                .enter()
                                    .append("g")
                                    .attr("class", "dependency")
                                    .attr("id", function(d){return d.index})
                                    .append("rect")
                                    .attr("width", proportions.boxSize)
                                    .attr("height", proportions.boxSize)
                                    .attr("x", function(d){ return d.x } )
                                    .attr("y", proportions.dependenciesHeight )
                                    .attr("rx", 5 )
                                    .attr("ry", 5 )
                                    .on("click", Actions.userClickOnAppTopologyDependency.bind(this));

        if(this.isDevModule() && this.props.configuration){
          AppendErrorIndicator(
            d3.selectAll('g.dependency'),
            this.props.configuration.dataServiceConfigurations,
            'dataServiceName',
            'no-versions-triangle',
            'dsbPlans',
            'circle',
            'no services',
            'dependency'
          );
        }

        var dependencyImgSize = this.isDevModule() ? 35 : proportions.boxSize / 2;

        var dependencyImg = d3.selectAll(".dependency")
                            .append("svg:image")
                            .attr("xlink:href",function(d){ return d.img })
                            .attr("width", dependencyImgSize)
                            .attr("height", dependencyImgSize)
                            .attr("class", "dependency" )
                            .attr("x", function(d){ return d.x + dependencyImgSize / 2 } )
                            .attr("y", proportions.dependenciesHeight + dependencyImgSize / 2 - 10)
                            .on("click", Actions.userClickOnAppTopologyDependency.bind(this))
                            .append("svg:title")
                                .text(function(d){
                                    return "type: " + d.type +
                                           " ,name: " + d.name
                                });

        if(!this.isDevModule()){
          var iconContainer = d3.selectAll("g.dependency")
                      .on("click", Actions.userClickOnAppTopologyDependency.bind(this))
                      .append("circle")
                      .attr("class", classNames.iconContainer)
                      .attr("cx", function(d){ return d.x } )
                      .attr("cy", proportions.dependenciesHeight )
        }

        var dependecyTitle = d3.selectAll("g.dependency")
                            .append('foreignObject')
                            .html((d)=>{return Config.getShortName(d.name, 10)})
                            .attr({
                              class: 'dependency-title',
                              x: d=>{return d.x},
                              y: proportions.dependenciesHeight +
                                  proportions.boxSize - 20,
                              width: proportions.boxSize
                            })
                            .append("svg:title")
                              .text(function(d){
                                return d.name
                              });


        var dependencyIconX = !this.isDevModule() ? 8 : -5,
            dependencyIconY = !this.isDevModule() ? 8 : -5;

        if(this.isDevModule() && this.props.configuration){
          AppendErrorIndicator(
            d3.selectAll('g.dependency'),
            this.props.configuration.dataServiceConfigurations,
            'dataServiceName',
            'no-plans-triangle',
            'dsbPlans',
            'triangle',
            'no services',
            'dependency'
          );
        }

        d3.xml(dbIcon).mimeType("image/svg+xml").get(function(error, xml) {
          if (error) throw error;

            $("g.dependency").each(function(index, element){
                var importedNode = document.importNode(xml.documentElement, true);
                var hasClass;
                var elementXvalue = $(element).children()[0].attributes.x.value,
                    elementYvalue = $(element).children()[0].attributes.y.value;

                importedNode.setAttribute("x", elementXvalue - dependencyIconX)
                importedNode.setAttribute("y", elementYvalue - dependencyIconY)
                importedNode.setAttribute("class", 'icon')

                _.forEach($(element), child=>{
                  _.forEach(child.children, child=>{
                    if(child.classList.contains('icon')){
                      hasClass = true;
                      return;
                    }
                  })
                });

                // if(!hasClass){
                    element.appendChild(importedNode);
                // }

                d3.select(element)
                .on("click", Actions.userClickOnAppTopologyDependency.bind(this));

            })

        });

        this.dependenciesContainerWidth = this.dependenciesContainer[0][0].getBoundingClientRect().width;

        Actions.setDependenciesContainerTransition(this.dependenciesContainerWidth);
        Actions.setServicesContainerTransition(this.servicesContainerWidth);

        this.basicPaths();

        //dependencies scroll buttons options
        var dependenciesScrollButtonsOptions = {
            buttonRadius: this.params().proportions.buttonRadius,
            buttonHeight: this.params().proportions.buttonHeight,
            elementsContainerWidth: this.dependenciesContainerWidth,
            classNames: {
                leftButtonContainer: classNames.btn + " " +
                                     classNames.btnContainerScrollDependenciesLeft,
                rightButtonContainer: classNames.btn + " " +
                                      classNames.btnContainerScrollDependenciesRight,
                leftButton: classNames.btnScrollDependenciesLeft,
                rightButton: classNames.btnScrollDependenciesRight
            },
            translateValue: 50,
            clickEvents: {
                translate: Actions.setDependenciesTranslate,
            },
            proportions: {
                leftButtonX: this.params().proportions.buttonRadius + 5,
                leftButtonY: this.params().proportions.containerHeight -
                             this.params().proportions.buttonRadius - 5,
                rightButtonX: this.params().proportions.containerWidth -
                              this.params().proportions.buttonRadius - 5,
                rightButtonY: this.params().proportions.containerHeight -
                              this.params().proportions.buttonRadius - 5,
            }
        }

        //services scroll buttons options
        var servicesScrollButtonsOptions = {
            buttonRadius: this.params().proportions.buttonRadius,
            elementsContainerWidth: this.servicesContainerWidth,
            classNames: {
                leftButtonContainer: classNames.btn + " " +
                                     classNames.btnContainerScrollServicesLeft,
                rightButtonContainer: classNames.btn + " " +
                                      classNames.btnContainerScrollServicesRight,
                leftButton: classNames.btnScrollServicesLeft,
                rightButton: classNames.btnScrollServicesRight
            },
            translateValue: 50,
            clickEvents: {
                translate: Actions.setServicesTranslate,
            },
            proportions: {
                leftButtonX: this.params().proportions.buttonRadius + 5,
                leftButtonY: this.params().proportions.buttonRadius + 5,
                rightButtonX: this.params().proportions.containerWidth -
                              this.params().proportions.buttonRadius - 5,
                rightButtonY: this.params().proportions.buttonRadius + 5,
            }
        }

        //create scroll buttons
        this.createTranslationButtons(dependenciesScrollButtonsOptions);
        this.createTranslationButtons(servicesScrollButtonsOptions);
    },

    createTranslationButtons: function(options){

		var buttonRadius = options.buttonRadius,
            width = this.params().proportions.containerWidth,
            height = this.params().proportions.containerHeight,
            translateValue = options.translateValue,
            elementsContainerWidth: options.elementsContainerWidth

        if(options.elementsContainerWidth > width){

            var leftButtonContainer = d3.select(".app-topology-container svg")
                                      .append("g")
                                      .attr("class", options.classNames.leftButtonContainer);

            var leftButton = leftButtonContainer.append("circle")
                                .attr("class", options.classNames.leftButton)
                                .attr("r", buttonRadius)
                                .attr("cx", options.proportions.leftButtonX)
                                .attr("cy", options.proportions.leftButtonY)
                                .attr("fill", "#e6eaed")
                                .on("click", options.clickEvents.translate.bind(this, -translateValue));

            leftButtonContainer
                .append("foreignObject")
                    .attr("class", "button scroll-left")
                    .attr("x", options.proportions.leftButtonX - buttonRadius - 85)
                    .attr("y", options.proportions.leftButtonY - buttonRadius + 25)
                .append("xhtml:div")
                    .html("<span></span><span></span>")
                    .on("click", options.clickEvents.translate.bind(this, -translateValue));

            var rightButtonContainer = d3.select(".app-topology-container svg")
                                      .append("g")
                                      .attr("class", options.classNames.rightButtonContainer);

            var rightButton = rightButtonContainer.append("circle")
                                .attr("class", options.classNames.rightButton)
                                .attr("r", buttonRadius)
                                .attr("cx", options.proportions.rightButtonX)
                                .attr("cy", options.proportions.rightButtonY)
                                .attr("fill", "#e6eaed")
                                .on("click", options.clickEvents.translate.bind(this, translateValue));

            rightButtonContainer
                .append("foreignObject")
                    .attr("class", "button scroll-right")
                    .attr("x", options.proportions.rightButtonX - buttonRadius - 35)
                    .attr("y", options.proportions.rightButtonY - buttonRadius + 25)
                .append("xhtml:div")
                    .html("<span></span><span></span>")
                    .on("click", options.clickEvents.translate.bind(this, translateValue));



        }
    },

    basicPaths: function(){

	    //set base line
      var baseline = lines.setBaseLine(this.params(), this.props.servicesTranslate);
      this.drawLine(baseline, this.basicPathParams(classNames.baseline));

  		//set basic lines from services to baseline
  		this.params().collections.services.forEach((service)=>{

			var servicesLine = lines.setServiceLine(service, this.params());
			this.drawLine(servicesLine, this.basicPathParams(classNames.serviceToBaseline));

		})

		//set basic lines from dependencies to baseline
		this.params().collections.dependencies.forEach((dependency)=>{

			var dependencyToBaseline = lines.setPathsFromBaseLineToDependencies(
														dependency,
														this.params()
									   );
			this.drawLine(dependencyToBaseline, this.basicPathParams(classNames.baselineToDependency));

		});

    },

    /****************************
     * Handle service selection *
     ****************************/

    highlightSelectedService: function(){

        var state = this.props.state,
            selectedService = state.services.selectedService.service,
            servicesElements = d3.select(".services-container").selectAll("rect")[0];

        if(servicesElements){
          servicesElements.forEach((serviceElement)=>{
             d3.select(serviceElement).attr("class", "service");
          });

          var selectedServiceElement = servicesElements[selectedService.serviceId];
          d3.select(selectedServiceElement)
              .attr("class", "service selected")
        }

    },

    removeSelectedServiceHighlight: function(){

        var servicesElements = d3.select(".services-container").selectAll("rect")[0];

        servicesElements.forEach((serviceElement)=>{
           d3.select(serviceElement).attr("class", "service");
        });

    },


    getClosestDependency: function(selectedService, firstDependency, lastDependency){

        var translate = this.props.state.dependencies.translate,
            servicesTranslate = this.props.servicesTranslate,
            selectedServiceX = selectedService.x + translate;

        if(selectedServiceX <= firstDependency.x){
            return firstDependency;
        }
        else if(selectedServiceX >= lastDependency.x){
            return lastDependency;
        }

    },

    /*******************************
     * Handle dependency selection *
     *******************************/

    highlightSelectedDependency: function(){

        var state = this.props.state,
            selectedDependency = state.dependencies.selectedDependency.dependency,
            dependenciesElements = d3.select(".dependencies-container").selectAll("rect")[0];

        dependenciesElements.forEach((dependencyElement)=>{
           d3.select(dependencyElement).attr("class", "dependency");
        });

        var selectedDependencyElement = dependenciesElements[selectedDependency.index];

        d3.select(selectedDependencyElement)
            .attr("class", "dependency selected");
    },

    removeSelectedDependencyHighlight: function(){

        var dependenciesElements = d3.select(".dependencies-container").selectAll("rect")[0];
        if(dependenciesElements){
          dependenciesElements.forEach((dependencyElement)=>{
             d3.select(dependencyElement).attr("class", "dependency");
          });
        }
    },

    /*********************************
     * Handle dependencies translate *
     *********************************/

    translateDependencies: function(){
        var transitionRate = 50,
            translate = -this.props.state.dependencies.translate,
            dot = ".";

        var baseline = dot + classNames.baseline,
            selectedPath = dot + classNames.selectedPath,
            basicPath = dot + classNames.basicPath,
            baselineToDependency = dot + classNames.baselineToDependency,
            firstToLastService = dot + classNames.firstToLastService,
            serviceToBaseline = dot + classNames.serviceToBaseline,
            serviceToClosestDependency = dot + classNames.serviceToClosestDependency,
			containerWidth = this.params().proportions.containerWidth;


        //translate container
        if(this.dependenciesContainer){
          this.dependenciesContainer
              .attr("transform", "translate("+ translate +",0)");
        }

        //basic path
        this.translate(basicPath + baseline, translate);
        this.translate(basicPath + baselineToDependency, translate);

        //selected path dependency selected
        this.translate(selectedPath, translate);

        //keep services line in place
        this.translate(serviceToBaseline, 0);

        //keep line from first to last related service in place
        this.translate(firstToLastService, 0);

		if(this.servicesContainerWidth > containerWidth &&
           this.dependenciesContainerWidth > containerWidth){

			this.translate(serviceToBaseline,
                           -this.props.servicesTranslate);
			this.translate(baselineToDependency,
                           -this.props.dependenciesTranslate);
            this.translate(serviceToClosestDependency,
                           -this.props.dependenciesTranslate-this.props.servicesTranslate);
			this.translate(firstToLastService,
                           -this.props.servicesTranslate)
		}

        this.baselineToFirstService();
        this.dependencyScrollBtnsVisibility(translate);
    },

    dependencyScrollBtnsVisibility: function(translate){
        var firstDependency = this.props.state.application.allDependencies[0],
            lastDependency = _.last(this.props.state.application.allDependencies),
            dot = ".";

        var shouldScrollLeft = firstDependency.x +
                               translate -
                               this.props.state.params.padding != 0;

        var shouldScrollRight = translate +
                                this.props.state.params.padding > firstDependency.x

        if(!shouldScrollLeft){
            d3.select(dot + classNames.btnContainerScrollDependenciesLeft)
                .attr("display", "none");
        }else{
            d3.select(dot + classNames.btnContainerScrollDependenciesLeft)
                .attr("display", "block");
        }

        if(!shouldScrollRight){
            d3.select(dot + classNames.btnContainerScrollDependenciesRight)
                .attr("display", "none");
        }else{
            d3.select(dot + classNames.btnContainerScrollDependenciesRight)
                .attr("display", "block");
        }
    },

    /*****************************
     * Handle services translate *
     *****************************/

    translateServices: function(){

        var transitionRate = 50,
            translate = -this.props.state.services.translate,
			state = this.props.state,
            dot = ".";

        var baseline = dot + classNames.baseline,
            selectedPath = dot + classNames.selectedPath,
            basicPath = dot + classNames.basicPath,
            baselineToDependency = dot + classNames.baselineToDependency,
			dependencyToBaseline = dot + classNames.dependencyToBaseline,
            serviceToBaseline = dot + classNames.serviceToBaseline,
            serviceToService = dot + classNames.serviceToService,
			serviceToClosestDependency = dot + classNames.serviceToClosestDependency,
			firstToLastService = dot + classNames.firstToLastService,
			containerWidth = this.params().proportions.containerWidth;

        if(this.servicesContainerWidth > containerWidth){

			this.servicesContainer
				.attr("transform", "translate("+ translate +",0)");

			//basic path
			if(state.application.allServices.length > state.application.allDependencies.length){
				this.translate(basicPath + baseline, translate);
			}

			this.translate(basicPath + baselineToDependency, 0);

			this.translate(firstToLastService, translate);
			this.translate(serviceToClosestDependency, translate);
			this.translate(baselineToDependency, 0);
			this.translate(dependencyToBaseline, 0);
			this.translate(serviceToClosestDependency, translate);
			this.translate(serviceToService, 0);

			//keep services line in place
			this.translate(serviceToBaseline, translate);

            this.servicesScrollBtnsVisibility(translate);
		}

		if(this.servicesContainerWidth > containerWidth &&
           this.dependenciesContainerWidth > containerWidth){

			this.translate(basicPath + baseline,
                           -this.props.dependenciesTranslate);
			this.translate(serviceToBaseline,
                           -this.props.servicesTranslate)
			this.translate(baselineToDependency,
                           -this.props.dependenciesTranslate)
			this.translate(dependencyToBaseline,
                           -this.props.dependenciesTranslate)
			this.translate(serviceToClosestDependency,
                           -this.props.dependenciesTranslate-this.props.servicesTranslate);

            this.baselineToFirstService();
		}
    },

    //complete base line if first service x
    //is larger than first dependency x
    baselineToFirstService: function(){

        var firstDependency = this.params().collections.dependencies[0],
            firstService = this.params().collections.services[0],
            dot = ".";

        var firstServiceX = firstService.x - this.props.servicesTranslate,
            firstDependencyX = firstDependency.x - this.props.dependenciesTranslate;

        this.removePathByClassName(dot + classNames.baselineToFirstService);

        if(firstDependencyX > firstServiceX){

            var baselineToFirstService =
                lines.baselineToFirstService(firstService,
                                             firstDependency,
                                             this.props.servicesTranslate,
                                             this.props.dependenciesTranslate,
                                             this.params().proportions);
            this.drawLine(baselineToFirstService, this.basicPathParams(classNames.baselineToFirstService))
        }else{
            this.removePathByClassName(dot + classNames.baselineToFirstService);
        }

    },

    servicesScrollBtnsVisibility: function(translate){
        var firstService = this.props.state.application.allServices[0],
            dot = ".";

        var shouldScrollLeft = firstService.x +
                               translate -
                               this.props.state.params.padding != 0;

        var shouldScrollRight = translate +
                                this.props.state.params.padding > firstService.x


        if(!shouldScrollLeft){
            d3.select(dot + classNames.btnContainerScrollServicesLeft).attr("display", "none");
        }else{
            d3.select(dot + classNames.btnContainerScrollServicesLeft).attr("display", "block");
        }

        if(!shouldScrollRight){
            d3.select(dot + classNames.btnContainerScrollServicesRight).attr("display", "none");
        }else{
            d3.select(dot + classNames.btnContainerScrollServicesRight).attr("display", "block");
        }
    },


    /******************
     * general params *
     ******************/

    drawLine: function(line, params){

      // if( isNaN(line[0]) || isNaN(line[1]) ){
      //   return;
      // }

      var lineFunction = d3.svg.line()
              .x(function(d){ return d.x })
              .y(function(d){ return d.y })
              .interpolate("step-before");

      d3.select(".app-topology-container svg")
              .append("path")
                  .attr("class", params.className)
                  .attr("stroke", params.stroke)
                  .attr("stroke-width", params.strokeWidth)
                  .attr("stroke-dasharray", params.strokeDasharray)
                  .attr("fill", params.fill)
                  .attr("d", lineFunction(line))

    },

    params: function(){
        var props = this.props.state;

        return {
            collections: {
                services: props.application.allServices,
                dependencies: props.application.allDependencies,
            },
            proportions: {
                containerWidth: this.props.containerWidth === undefined ||
                                this.props.containerWidth === null ? props.params.containerWidth : this.props.containerWidth,
                containerHeight: this.props.containerHeight === undefined ||
                                this.props.containerHeight === null ? props.params.containerHeight : this.props.containerHeight,
                baseLineHeight: props.params.baseLineHeight,
                servicesHeight: props.params.servicesHeight,
                dependenciesHeight: props.params.dependenciesHeight,
                dependenciesTranslate: props.dependencies.translate,
                servicesTranslate: props.services.translate,
                boxSize: props.params.boxSize,
                buttonRadius: 20
            }
        }

    },

    //params for basic path
    basicPathParams: function(className){
        className = className == undefined ? "" : " " + className;
        return {
            className: classNames.basicPath + className,
            stroke: "#8dc5e4",
            strokeWidth: "2px",
            strokeDasharray: [3,3],
            fill: "#000"
        }
    },

    //params for selected path
    selectedPathParams: function(className){
      className = className == undefined ? "" : " " + className;
      return {
          className: classNames.selectedPath + className,
          stroke: "#8dc5e4",
          strokeWidth: "3px",
          strokeDasharray: [0,0],
          fill: "red"
      }
    },

    getClosestElement: function(selectedElement, firstTarget, lastTarget){
        var diff1 = selectedElement.x + firstTarget.x;
        var diff2 = selectedElement.x + lastTarget.x;

        return diff1 > diff2 ? firstTarget : lastTarget;
    },

    removeSelectedPath: function(){
        d3.selectAll(".selected-path").remove();
    },

    removePathByClassName: function(className){
        d3.selectAll(className).remove();
    },

    translate: function(className, translate, animation){
        d3.selectAll(className)
            .attr("transform", "translate("+ translate +",0)");
    },

	render: function() {
		return (
            <div>
                <div className={"app-topology-container " + this.props.currentStepName}></div>
                <div className="services-label">
                    <img src={LabelA} />
                    <span>app services</span>
                </div>
                <div className="dependencies-label">
                    <img src={LabelB} />
                    <span>infrastructure service</span>
                </div>

            </div>
		)
	}
});

export default AppTopology;
