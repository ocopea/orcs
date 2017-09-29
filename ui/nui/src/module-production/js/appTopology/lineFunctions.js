// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
var lineFunctions = {

	setBaseLine: function(params, servicesTranslate){
		if(params != undefined){

			var firstDependency = params.collections.dependencies[0],
				lastDependency = _.last(params.collections.dependencies),
				firstService = params.collections.services[0],
				lastService = _.last(params.collections.services),
				line = [];

			if(params.collections.services.length <= params.collections.dependencies.length){
				line = [
					{
						"x": firstDependency.x + params.proportions.boxSize/2,
						"y": params.proportions.baseLineHeight
					},
					{
						"x": lastDependency.x + params.proportions.boxSize/2,
						"y": params.proportions.baseLineHeight
					},
				]
			}else{
				line = [
					{
						"x": firstService.x + servicesTranslate + params.proportions.boxSize/2,
						"y": params.proportions.baseLineHeight
					},
					{
						"x": lastService.x + params.proportions.boxSize/2,
						"y": params.proportions.baseLineHeight
					},
				]
			}
			return line;

		}

	},

    baselineToFirstService: function(firstService,
                                      firstDependency,
                                      servicesTranslate,
                                      dependenciesTranslate,
                                      proportions){

        var line = [
            {"x": firstDependency.x - dependenciesTranslate + proportions.boxSize/2 ,"y": proportions.baseLineHeight},
            {"x": firstService.x - servicesTranslate + proportions.boxSize/2, "y": proportions.baseLineHeight}
        ];
        return line;
    },

	setServiceLine: function(service, params){

		var line = [
			{
				"x": service.x + params.proportions.boxSize/2,
				"y": params.proportions.servicesHeight + params.proportions.boxSize
			},
			{
				"x": service.x + params.proportions.boxSize/2,
				"y": params.proportions.baseLineHeight
			}
		]

		return line;
	},

	setPathsFromBaseLineToDependencies: function(dependency, params){

		var line = [
			{
				"x": dependency.x + params.proportions.boxSize/2,
				"y": params.proportions.baseLineHeight
			},
			{
				"x": dependency.x + params.proportions.boxSize/2,
				"y": params.proportions.dependenciesHeight
			}
		]
		return line;
	},

	/**
	 * Handle service selection
	 */

	pathFromSelectedServiceToBaseline: function(selectedService, params){

        var line = [
            {
                "x": selectedService.x + params.proportions.boxSize/2,
                "y": params.proportions.servicesHeight + params.proportions.boxSize
            },
            {
                "x": selectedService.x + params.proportions.boxSize/2,
                "y": params.proportions.baseLineHeight
            }
        ]
		return line;
	},

	pathFromFirstToLastRelatedDependencies: function(firstRelatedDependency,
													 lastRelatedDependency,
													 params){

		var line = [
			{
				"x": firstRelatedDependency.x + params.proportions.boxSize/2,
				"y": params.proportions.baseLineHeight
			},
			{
				"x": lastRelatedDependency.x + params.proportions.boxSize/2,
				"y": params.proportions.baseLineHeight
			}
		];
		return line;
	},

	pathFromBaseLineToRelatedDependency: function(dependency, params){

		var line = [
			{
				"x": dependency.x + params.proportions.boxSize/2,
				"y": params.proportions.baseLineHeight
			},
			{
				"x": dependency.x + params.proportions.boxSize/2,
				"y": params.proportions.dependenciesHeight
			}
		]
		return line;
	},

	pathFromServiceToClosestRelatedDependency: function(selectedService,
														firstRelatedDependency,
														lastRelatedDependency,
														closestRelatedDependency,
														dependenciesTranslate,
														servicesTranslate,
													 	params
											   ){
		var line = [],
			selectedService = selectedService.service;

		if(selectedService.x + dependenciesTranslate >
           lastRelatedDependency.x + servicesTranslate ||
           selectedService.x + dependenciesTranslate <
           firstRelatedDependency.x + servicesTranslate ){

            line = [
                {
					"x": selectedService.x +
                         dependenciesTranslate +
                         params.proportions.boxSize/2
					,"y": params.proportions.baseLineHeight
				},
                {
					"x": closestRelatedDependency.x +
                         params.proportions.boxSize/2 +
                         servicesTranslate,
					"y": params.proportions.baseLineHeight
				}
            ]

        }

		return line;
	},

	/**
	 *	Handle dependency selection
	 */

	pathFromSelectedDependencyToBaseLine: function(selectedDependency, params){
		
        var line = [
            {
                "x": selectedDependency.x + params.proportions.boxSize/2,
                "y": params.proportions.dependenciesHeight
            },
            {
                "x": selectedDependency.x + params.proportions.boxSize/2,
                "y": params.proportions.baseLineHeight
            }
        ]
		return line;
	},

	pathFromRelatedServiceToBaseline: function(service, params){

		var line = [
			{
				"x": service.x + params.proportions.boxSize/2,
				"y": params.proportions.servicesHeight + params.proportions.boxSize
			},
			{
				"x": service.x + params.proportions.boxSize/2,
				"y": params.proportions.baseLineHeight
			}
		]
		return line;
	},

	pathFromFirstToLastRelatedServices: function(firstRelatedService,
                                                 lastRelatedService, params){

        var line = [
            {
                "x": firstRelatedService.x + params.proportions.boxSize/2,
                "y": params.proportions.baseLineHeight
            },
            {
                "x": lastRelatedService.x + params.proportions.boxSize/2,
                "y": params.proportions.baseLineHeight
            }
        ]
		return line;
	},

	pathFromSelectedDependencyToClosestService: function(selectedDependency,
														 closestService,
														 dependenciesTranslate,
														 servicesTranslate,
														 params){
		var line = [
			{
				"x": selectedDependency.x + params.proportions.boxSize/2,
				"y": params.proportions.baseLineHeight
			},
			{
				"x": closestService.x -
                     servicesTranslate +
                     params.proportions.boxSize/2 +
                     dependenciesTranslate,
				"y": params.proportions.baseLineHeight
			}
		]
		return line;
	},

}

export default lineFunctions;
