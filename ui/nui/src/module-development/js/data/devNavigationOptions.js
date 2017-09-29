// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
var DevelopmentNavigationOptions = {
	module: "development",
	wizard: {
		location: "/wizard",
		subLocation: {
			appMarket: "/App-Market",
			image: "/image",
			config: "/config"
		}
	},
	dashboard: {
		location: "/dashboard"
	},
	main: {
		location: "/main",
		subLocations: {
			quotasList: "/quotas-list"
		}
	},
	savedImages: {
		location: "/saved-images"
	},
	deployingProgress: {
		location: "/deploying-progress"
	},
	sharedImage: {
		location: "/shared-image"
	},
	settings: {
		location: "/settings",
		subLocation: {
			integrationsConfig: {
				location: '/integrations-config',
				subLocation: {
					jira: '/jira',
					trello: '/trello',
					email: '/email'
				}
			},
			permissions: {
				location: "/permissions"
			}
		}
	},
	siteConfig: {
		location: '/site-config',
		subLocation: {
			addArtifact: '/add-artifact',
			save: '/save',
			dsbDetailsDialog: '/details',
			addCr: '/add-cr'
		}
	}

};

export default DevelopmentNavigationOptions;
