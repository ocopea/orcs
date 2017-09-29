// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package restapi

import (
	"crypto/tls"
	"net/http"

	errors "github.com/go-openapi/errors"
	runtime "github.com/go-openapi/runtime"
	middleware "github.com/go-openapi/runtime/middleware"
	graceful "github.com/tylerb/graceful"

	"../../work/nazgul/copyrepo/copyrepo-web-api/src/main/resources/restapi/operations"
	"../../work/nazgul/copyrepo/copyrepo-web-api/src/main/resources/restapi/operations/crb_web"
	"../../work/nazgul/copyrepo/copyrepo-web-api/src/main/resources/restapi/operations/crb_web_data"
)

// This file is safe to edit. Once it exists it will not be overwritten

//go:generate swagger generate server --target .. --name crb --spec ../swagger.yaml

func configureFlags(api *operations.CrbAPI) {
	// api.CommandLineOptionsGroups = []swag.CommandLineOptionsGroup{ ... }
}

func configureAPI(api *operations.CrbAPI) http.Handler {
	// configure the api here
	api.ServeError = errors.ServeError

	// Set your custom logger if needed. Default one is log.Printf
	// Expected interface func(string, ...interface{})
	//
	// Example:
	// s.api.Logger = log.Printf

	api.JSONConsumer = runtime.JSONConsumer()

	api.BinConsumer = runtime.ByteStreamConsumer()

	api.JSONProducer = runtime.JSONProducer()

	api.BinProducer = runtime.ByteStreamProducer()

	api.CrbWebDataCreateCopyInRepoHandler = crb_web_data.CreateCopyInRepoHandlerFunc(func(params crb_web_data.CreateCopyInRepoParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web_data.CreateCopyInRepo has not yet been implemented")
	})
	api.CrbWebDeleteCopyHandler = crb_web.DeleteCopyHandlerFunc(func(params crb_web.DeleteCopyParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.DeleteCopy has not yet been implemented")
	})
	api.CrbWebDeleteRepositoryHandler = crb_web.DeleteRepositoryHandlerFunc(func(params crb_web.DeleteRepositoryParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.DeleteRepository has not yet been implemented")
	})
	api.CrbWebGetCopyInstancesHandler = crb_web.GetCopyInstancesHandlerFunc(func(params crb_web.GetCopyInstancesParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.GetCopyInstances has not yet been implemented")
	})
	api.CrbWebGetCopyMetaDataHandler = crb_web.GetCopyMetaDataHandlerFunc(func(params crb_web.GetCopyMetaDataParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.GetCopyMetaData has not yet been implemented")
	})
	api.CrbWebGetInfoHandler = crb_web.GetInfoHandlerFunc(func(params crb_web.GetInfoParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.GetInfo has not yet been implemented")
	})
	api.CrbWebGetRepositoryInfoHandler = crb_web.GetRepositoryInfoHandlerFunc(func(params crb_web.GetRepositoryInfoParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.GetRepositoryInfo has not yet been implemented")
	})
	api.CrbWebGetRepositoryStatsHandler = crb_web.GetRepositoryStatsHandlerFunc(func(params crb_web.GetRepositoryStatsParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.GetRepositoryStats has not yet been implemented")
	})
	api.CrbWebListRepositoryInstancesHandler = crb_web.ListRepositoryInstancesHandlerFunc(func(params crb_web.ListRepositoryInstancesParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.ListRepositoryInstances has not yet been implemented")
	})
	api.CrbWebDataRetrieveCopyHandler = crb_web_data.RetrieveCopyHandlerFunc(func(params crb_web_data.RetrieveCopyParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web_data.RetrieveCopy has not yet been implemented")
	})
	api.CrbWebStoreCopyMetaDataHandler = crb_web.StoreCopyMetaDataHandlerFunc(func(params crb_web.StoreCopyMetaDataParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.StoreCopyMetaData has not yet been implemented")
	})
	api.CrbWebStoreRepositoryInfoHandler = crb_web.StoreRepositoryInfoHandlerFunc(func(params crb_web.StoreRepositoryInfoParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.StoreRepositoryInfo has not yet been implemented")
	})
	api.CrbWebUpdateRepositoryInfoHandler = crb_web.UpdateRepositoryInfoHandlerFunc(func(params crb_web.UpdateRepositoryInfoParams) middleware.Responder {
		return middleware.NotImplemented("operation crb_web.UpdateRepositoryInfo has not yet been implemented")
	})

	api.ServerShutdown = func() {}

	return setupGlobalMiddleware(api.Serve(setupMiddlewares))
}

// The TLS configuration before HTTPS server starts.
func configureTLS(tlsConfig *tls.Config) {
	// Make all necessary changes to the TLS configuration here.
}

// As soon as server is initialized but not run yet, this function will be called.
// If you need to modify a config, store server instance to stop it individually later, this is the place.
// This function can be called multiple times, depending on the number of serving schemes.
// scheme value will be set accordingly: "http", "https" or "unix"
func configureServer(s *graceful.Server, scheme, addr string) {
}

// The middleware configuration is for the handler executors. These do not apply to the swagger.json document.
// The middleware executes after routing but before authentication, binding and validation
func setupMiddlewares(handler http.Handler) http.Handler {
	return handler
}

// The middleware configuration happens before anything, this middleware also applies to serving the swagger.json document.
// So this is a good place to plug in a panic handling middleware, logging and metrics
func setupGlobalMiddleware(handler http.Handler) http.Handler {
	return handler
}
