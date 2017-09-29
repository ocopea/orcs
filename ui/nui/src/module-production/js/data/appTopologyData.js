// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
var data = 
[{
	"id": "bfab75f6-4151-4c21-a206-8b80b1e4429f",
	"name": "hackathon",
	"version": "1.0",
	"description": "Hackathon Registration App",
	"appServiceTemplates": [{
		"name": "hackathon",
		"version": "1.0",
		"psbType": "ShpanPaaS",
		"dependencies": [{
			"type": "postgres",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s3",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		},{
			"type": "ss",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s3",
			"name": "ddsd",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		},{
			"type": "ssdsdsds",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "ddsdsdsdsd",
			"name": "ddsd",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		},{
			"type": "d",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s",
			"name": "ddsd",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		},{
			"type": "sdsdssss",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "we",
			"name": "ddsd",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "sdswewqdssss",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "qqq",
			"name": "ddsd",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, ],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-hackathon.png"
	}],
	"entryPointServiceURI": "hackathon",
	"entryPointServicePath": "hackathon-api/html/nui/index.html",
	"img": "http://localhost:8080/hub-web-api/html/img/app-template/hackathon.png",
	"isSelected": false
}, {
	"id": "bfab75f6-4151-4c21-a206-8b80b1e4429f",
	"name": "hackathon",
	"version": "1.0",
	"description": "Hackathon Registration App",
	"appServiceTemplates": [{
		"name": "hackathon",
		"version": "1.0",
		"psbType": "ShpanPaaS",
		"dependencies": [{
			"type": "postgres",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s3",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-hackathon.png"
	}],
	"entryPointServiceURI": "hackathon",
	"entryPointServicePath": "hackathon-api/html/nui/index.html",
	"img": "http://localhost:8080/hub-web-api/html/img/app-template/hackathon.png",
	"isSelected": false
}, {
	"id": "f868fc0f-59b5-4778-9073-bcc1ab0a8386",
	"name": "wordpress",
	"version": "1.0",
	"description": "Wordpress content management",
	"appServiceTemplates": [{
		"name": "wordpress",
		"version": "1.0",
		"psbType": "ShpanPaaS",
		"dependencies": [{
			"type": "mysql",
			"name": "configuration",
			"description": "wordpress configuration db",
			"img": "../img/infra-svc-mysql.png"
		}, {
			"type": "docker-volume",
			"name": "documents",
			"description": "Wordpress documents",
			"img": "../img/infra-svc-docker-volume.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-wordpress.png"
	}],
	"entryPointServiceURI": "wordpress",
	"entryPointServicePath": "",
	"img": "http://localhost:8080/hub-web-api/html/img/app-template/wordpress.png",
	"isSelected": false
}, {
	"id": "e3d1a60e-16d6-450a-ab72-5054a466db91",
	"name": "hackathon-pro",
	"version": "1.0",
	"description": "Hackathon Pro Registration App",
	"appServiceTemplates": [{
		"name": "hackathon",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "postgres",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s3",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-hackathon.png"
	}, {
		"name": "submission evaluator",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}],
	"entryPointServiceURI": "hackathon",
	"entryPointServicePath": "hackathon-api/html",
	"img": "http://localhost:8080/hub-web-api/html/img/app-template/hackathon-pro.png",
	"isSelected": false
}, {
	"id": "54a5f1b4-8823-40c8-9468-cd9dfad289eb",
	"name": "complex-app",
	"version": "1.0",
	"description": "Complex app that looks like the hackathon",
	"appServiceTemplates": [{
		"name": "hackathon",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "postgres",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s3",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-hackathon.png"
	}, {
		"name": "submission evaluator",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}],
	"entryPointServiceURI": "hackathon",
	"entryPointServicePath": "hackathon-api/html",
	"img": "http://localhost:8080/hub-web-api/html/img/app-template/complex-app.png",
	"isSelected": false
}, {
	"id": "4214488d-887d-4141-8cb2-5ad65e71f53e",
	"name": "complex-app2",
	"version": "1.0",
	"description": "Complex app2 that looks like the hackathon",
	"appServiceTemplates": [{
		"name": "hackathon",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "postgres",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "postgres",
			"name": "configuration-db",
			"description": "Hackathon configuration",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s3",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-hackathon.png"
	}, {
		"name": "submission evaluator",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}, {
			"type": "mongo",
			"name": "profile-db",
			"description": "Mongo db",
			"img": "../img/infra-svc-mongo.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}, {
		"name": "gals nightmare",
		"version": "1.1",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}, {
			"type": "postgres",
			"name": "configuration-db",
			"description": "Hackathon configuration",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "mysql",
			"name": "customers-db",
			"description": "Customers database",
			"img": "../img/infra-svc-mysql.png"
		}, {
			"type": "mongo",
			"name": "profile-db",
			"description": "Mongo db",
			"img": "../img/infra-svc-mongo.png"
		}, {
			"type": "gal",
			"name": "profile-db",
			"description": "gal's db",
			"img": "../img/infra-svc-mongo.png"
		}, {
			"type": "gal",
			"name": "customers-db",
			"description": "Customers database",
			"img": "../img/infra-svc-mysql.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-gals nightmare.png"
	}],
	"entryPointServiceURI": "hackathon",
	"entryPointServicePath": "hackathon-api/html",
	"img": "http://localhost:8080/hub-web-api/html/img/app-template/complex-app2.png",
	"isSelected": false
},  {
	"id": "54a5f1b4-8823-40c8-9468-cd9dfad289eb",
	"name": "many services",
	"version": "1.0",
	"description": "handeling many services scenerio",
	"appServiceTemplates": [{
		"name": "hackathon",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "postgres",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s3",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-hackathon.png"
	}, {
		"name": "submission evaluator",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}, {
		"name": "another service",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}, {
		"name": "submission evaluator",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img":  "../img/infra-svc-postgres.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}, {
		"name": "another service",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}, {
		"name": "another service",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}, {
		"name": "another service",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}, {
		"name": "another service",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}],
	"entryPointServiceURI": "hackathon",
	"entryPointServicePath": "hackathon-api/html",
	"img": "http://localhost:8080/hub-web-api/html/img/app-template/complex-app.png",
	"isSelected": false
},
{
	"id": "4214488d-887d-4141-8cb2-5ad65e71f53e",
	"name": "multiple dependencies and services",
	"version": "1.0",
	"description": "multiple dependencies and services",
	"appServiceTemplates": [{
		"name": "hackathon",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "postgres",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "postgres",
			"name": "configuration-db",
			"description": "Hackathon configuration",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s3",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		},{
			"type": "aaa",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "bbb",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		},{
			"type": "ccc",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "ddd",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-hackathon.png"
	},{
		"name": "hackathon",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "ddsdsd",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "dsdsds",
			"name": "configuration-db",
			"description": "Hackathon configuration",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "wewewe",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "rwrw",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		},{
			"type": "wrwr",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "dfdg",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		},{
			"type": "dgds",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "sdgs",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-hackathon.png"
	}, {
		"name": "hackathon",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "s",
			"name": "hackathon-db",
			"description": "Hackathon ideas DB",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "ds",
			"name": "configuration-db",
			"description": "Hackathon configuration",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "s",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "rwrw",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		},{
			"type": "wrwr",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "dfdg",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		},{
			"type": "dgds",
			"name": "hack-docs",
			"description": "Hackathon document store",
			"img": "../img/infra-svc-s3.png"
		}, {
			"type": "sdgs",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-hackathon.png"
	},{
		"name": "submission evaluator",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}, {
			"type": "mongo",
			"name": "profile-db",
			"description": "Mongo db",
			"img": "../img/infra-svc-mongo.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	},{
		"name": "submission aaaa",
		"version": "1.0",
		"psbType": "k8s",
		"dependencies": [{
			"type": "d",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}, {
			"type": "a",
			"name": "profile-db",
			"description": "Mongo db",
			"img": "../img/infra-svc-mongo.png"
		},{
			"type": "ggg",
			"name": "profile-db",
			"description": "Mongo db",
			"img": "../img/infra-svc-mongo.png"
		},{
			"type": "qqq",
			"name": "profile-db",
			"description": "Mongo db",
			"img": "../img/infra-svc-mongo.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-submission evaluator.png"
	}, {
		"name": "gals nightmare",
		"version": "1.1",
		"psbType": "k8s",
		"dependencies": [{
			"type": "rabbitmq",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}, {
			"type": "postgres",
			"name": "configuration-db",
			"description": "Hackathon configuration",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "mysql",
			"name": "customers-db",
			"description": "Customers database",
			"img": "../img/infra-svc-mysql.png"
		}, {
			"type": "mongo",
			"name": "profile-db",
			"description": "Mongo db",
			"img": "../img/infra-svc-mongo.png"
		}, {
			"type": "gal",
			"name": "profile-db",
			"description": "gal's db",
			"img": "../img/infra-svc-mongo.png"
		}, {
			"type": "gal",
			"name": "customers-db",
			"description": "Customers database",
			"img": "../img/infra-svc-mysql.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-gals nightmare.png"
	}, {
		"name": "fff",
		"version": "1.1",
		"psbType": "k8s",
		"dependencies": [{
			"type": "dd",
			"name": "messaging",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}, {
			"type": "s",
			"name": "configuration-db",
			"description": "Hackathon configuration",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "rrr",
			"name": "customers-db",
			"description": "Customers database",
			"img": "../img/infra-svc-mysql.png"
		}, {
			"type": "tt",
			"name": "profile-db",
			"description": "Mongo db",
			"img": "../img/infra-svc-mongo.png"
		}, {
			"type": "yyy",
			"name": "profile-db",
			"description": "gal's db",
			"img": "../img/infra-svc-mongo.png"
		}, {
			"type": "zzz",
			"name": "customers-db",
			"description": "Customers database",
			"img": "../img/infra-svc-mysql.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-gals nightmare.png"
	},{
		"name": "q",
		"version": "1.1",
		"psbType": "k8s",
		"dependencies": [{
			"type": "dd",
			"name": "ssss",
			"description": "Messaging",
			"img": "../img/infra-svc-rabbitmq.png"
		}, {
			"type": "sdsd",
			"name": "configuration-db",
			"description": "Hackathon configuration",
			"img": "../img/infra-svc-postgres.png"
		}, {
			"type": "ttt",
			"name": "customers-db",
			"description": "Customers database",
			"img": "../img/infra-svc-mysql.png"
		}, {
			"type": "ee",
			"name": "profile-db",
			"description": "Mongo db",
			"img": "../img/infra-svc-mongo.png"
		}, {
			"type": "rr",
			"name": "profile-db",
			"description": "gal's db",
			"img": "../img/infra-svc-mongo.png"
		}, {
			"type": "ttyy",
			"name": "customers-db",
			"description": "Customers database",
			"img": "../img/infra-svc-mysql.png"
		}],
		"exposedPorts": [8080],
		"httpPort": 8080,
		"img": "../img/app-svc-gals nightmare.png"
	}],
	"entryPointServiceURI": "hackathon",
	"entryPointServicePath": "hackathon-api/html",
	"img": "http://localhost:8080/hub-web-api/html/img/app-template/complex-app2.png",
	"isSelected": false
}

]
export default data;